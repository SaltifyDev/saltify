package org.ntqqrev.milky

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ntqqrev.milky.entity.MilkyAnnouncement
import org.ntqqrev.milky.entity.MilkyFileEntry
import org.ntqqrev.milky.entity.MilkyFolderEntry
import org.ntqqrev.milky.entity.MilkyFriend
import org.ntqqrev.milky.entity.MilkyGroup
import org.ntqqrev.milky.entity.MilkyGroupMember
import org.ntqqrev.milky.exception.MilkyApiNotFoundException
import org.ntqqrev.milky.exception.MilkyBadCredentialsException
import org.ntqqrev.milky.exception.MilkyException
import org.ntqqrev.milky.message.*
import org.ntqqrev.milky.protocol.api.*
import org.ntqqrev.milky.protocol.event.*
import org.ntqqrev.milky.protocol.message.MilkyIncomingMessageData
import org.ntqqrev.milky.util.toEvent
import org.ntqqrev.milky.util.toMilkyMessageScene
import org.ntqqrev.milky.util.toMilkyUri
import org.ntqqrev.milky.util.toSaltifyMessageScene
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.event.*
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.ForwardedIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.MessageSendResult
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.message.outgoing.ResourceLocation
import org.ntqqrev.saltify.model.group.Announcement
import org.ntqqrev.saltify.model.group.FileSystemEntry
import kotlin.properties.Delegates

class MilkyContext internal constructor(
    internal val init: MilkyInit,
    internal val env: Environment,
    internal val flow: MutableSharedFlow<Event>,
) : Context {
    internal val logger = KotlinLogging.logger { }
    private val objectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    private val apiBaseUrl = (if (init.useHttps) "https://" else "http://") +
        (if (init.milkyUrl.endsWith("/")) init.milkyUrl else init.milkyUrl + "/") + "api"

    private val wsUrl = (if (init.useHttps) "wss://" else "ws://") +
        (if (init.milkyUrl.endsWith("/")) init.milkyUrl else init.milkyUrl + "/") +
        (if (init.milkyAccessToken.isEmpty()) "event" else "event?access_token=${init.milkyAccessToken}")

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = JacksonWebsocketContentConverter(objectMapper)
        }
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }
    }

    private var connectSeq = 0

    internal var instanceState by Delegates.observable(Context.State.INITIALIZED) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            env.scope.launch {
                flow.emit(
                    ContextStateChangeEvent(
                        this@MilkyContext,
                        Clock.System.now(),
                        oldValue,
                        newValue
                    )
                )
            }
        }
    }
    override val state: Context.State
        get() = instanceState

    private val friendCache = MilkyFriend.Cache(this)
    private val groupCache = MilkyGroup.Cache(this)

    override suspend fun start() {
        while (env.scope.isActive) {
            try {
                client.webSocket(wsUrl) {
                    connectSeq++
                    logger.info { "Connected to $wsUrl" }
                    instanceState = Context.State.STARTED
                    while (isActive) {
                        try {
                            val event = receiveDeserialized<MilkyEvent>()
                            launch {
                                try {
                                    processEvent(event)
                                } catch (e: Exception) {
                                    logger.error(e) { "Failed to process event" }
                                }
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to deserialize event" }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "WebSocket connection lost due to exception" }
            }
            instanceState = Context.State.INTERRUPTED
            delay(init.wsReconnectInterval)
            logger.info { "Reconnecting to $wsUrl..." }
        }
    }

    internal suspend inline fun <reified T, reified R> callApi(name: String, body: T): R {
        val response = client.post("${apiBaseUrl}/$name") {
            contentType(ContentType.Application.Json)
            if (init.milkyAccessToken.isNotEmpty()) {
                bearerAuth(init.milkyAccessToken)
            }
            setBody(body)
        }
        if (response.status.value != 200) {
            if (response.status.value == 401) {
                throw MilkyBadCredentialsException()
            }
            if (response.status.value == 404) {
                throw MilkyApiNotFoundException(name)
            }
            throw MilkyException("API call failed with status ${response.status.value}")
        }
        val callResult = response.body<JsonNode>()
        val retcode = callResult.get("retcode").asInt()
        if (retcode != 0) {
            throw MilkyException("API '$name' failed with code $retcode: ${callResult.get("message").asText()}")
        } else {
            return objectMapper.treeToValue(callResult.get("data"))
        }
    }

    internal suspend fun MilkyIncomingMessageData.toSaltifyMessage(): IncomingMessage {
        val message = when (this.messageScene.toSaltifyMessageScene()) {
            MessageScene.FRIEND ->
                MilkyIncomingPrivateMessage.fromFriendMessage(this@MilkyContext, this)

            MessageScene.GROUP ->
                MilkyIncomingGroupMessage.fromGroupMessage(this@MilkyContext, this)

            else -> throw MilkyException("Unsupported message type: ${this::class.simpleName}")
        }
        if (message == null) {
            throw MilkyException("Failed to parse message data (peerId=$peerId, seq=$messageSeq)")
        }
        return message
    }

    override suspend fun stop() {
        client.close()
        instanceState = Context.State.STOPPED
    }

    private var lastLoginInfo: Pair<Int, MilkyGetLoginInfoResponse>? = null

    override suspend fun getLoginInfo(): Pair<Long, String> {
        val response: MilkyGetLoginInfoResponse
        if (connectSeq == lastLoginInfo?.first) {
            response = lastLoginInfo!!.second
        } else {
            response = callApi<MilkyApiEmptyRequest, MilkyGetLoginInfoResponse>(
                "get_login_info",
                MilkyApiEmptyRequest()
            )
            lastLoginInfo = connectSeq to response
        }
        return response.uin to response.nickname
    }

    override suspend fun getAllFriends(cacheFirst: Boolean): Iterable<MilkyFriend> =
        friendCache.getAll(cacheFirst)

    override suspend fun getFriend(friendUin: Long, cacheFirst: Boolean): MilkyFriend? =
        friendCache.get(friendUin, cacheFirst)

    override suspend fun getAllGroups(cacheFirst: Boolean): Iterable<MilkyGroup> =
        groupCache.getAll(cacheFirst)

    override suspend fun getGroup(groupUin: Long, cacheFirst: Boolean): MilkyGroup? =
        groupCache.get(groupUin, cacheFirst)

    override suspend fun getAllGroupMembers(
        groupUin: Long,
        cacheFirst: Boolean
    ): Iterable<MilkyGroupMember> =
        getGroup(groupUin)?.groupMemberCache?.getAll(cacheFirst) ?: emptyList()

    override suspend fun getGroupMember(
        groupUin: Long,
        memberUin: Long,
        cacheFirst: Boolean
    ): MilkyGroupMember? =
        getGroup(groupUin)?.groupMemberCache?.get(memberUin, cacheFirst)

    override suspend fun sendPrivateMessage(
        userUin: Long,
        builder: PrivateMessageBuilder.() -> Unit
    ): MessageSendResult {
        val messageBuilder = MilkyUniversalMessageBuilder(this)
        messageBuilder.builder()
        val segments = messageBuilder.build()
        val response = callApi<MilkySendPrivateMessageRequest, MilkySendPrivateMessageResponse>(
            "send_private_message",
            MilkySendPrivateMessageRequest(
                userId = userUin,
                message = segments
            )
        )
        return MilkyMessageSendResult(
            ctx = this,
            sequence = response.messageSeq,
            time = Instant.fromEpochSeconds(response.time),
        )
    }

    override suspend fun sendGroupMessage(
        groupUin: Long,
        builder: GroupMessageBuilder.() -> Unit
    ): MessageSendResult {
        val messageBuilder = MilkyUniversalMessageBuilder(this)
        messageBuilder.builder()
        val segments = messageBuilder.build()
        val response = callApi<MilkySendGroupMessageRequest, MilkySendGroupMessageResponse>(
            "send_group_message",
            MilkySendGroupMessageRequest(
                groupId = groupUin,
                message = segments
            )
        )
        return MilkyMessageSendResult(
            ctx = this,
            sequence = response.messageSeq,
            time = Instant.fromEpochSeconds(response.time),
        )
    }

    override suspend fun getMessage(
        messageScene: MessageScene,
        peerId: Long,
        sequence: Long
    ): IncomingMessage =
        callApi<MilkyGetMessageRequest, MilkyGetMessageResponse>(
            "get_message",
            MilkyGetMessageRequest(
                messageScene = messageScene.toMilkyMessageScene(),
                peerId = peerId,
                messageSeq = sequence
            )
        ).message.toSaltifyMessage()

    override suspend fun getHistoryMessages(
        messageScene: MessageScene,
        peerId: Long,
        startSequence: Long?,
        isBackward: Boolean,
        limit: Int
    ): List<IncomingMessage> =
        callApi<MilkyGetHistoryMessagesRequest, MilkyGetHistoryMessagesResponse>(
            "get_history_messages",
            MilkyGetHistoryMessagesRequest(
                messageScene = messageScene.toMilkyMessageScene(),
                peerId = peerId,
                startMessageSeq = startSequence,
                direction = if (isBackward) "older" else "newer",
                limit = limit
            )
        ).messages.map { it.toSaltifyMessage() }

    override suspend fun getResourceTempUrl(resourceId: String): String =
        callApi<MilkyGetResourceTempUrlRequest, MilkyGetResourceTempUrlResponse>(
            "get_resource_temp_url",
            MilkyGetResourceTempUrlRequest(resourceId)
        ).url

    override suspend fun getForwardedMessages(forwardId: String): List<ForwardedIncomingMessage> =
        callApi<MilkyGetForwardedMessagesRequest, MilkyGetForwardedMessagesResponse>(
            "get_forwarded_messages",
            MilkyGetForwardedMessagesRequest(forwardId)
        ).messages.map { MilkyIncomingForwardedMessage.fromData(this, it) }

    override suspend fun recallPrivateMessage(userUin: Long, sequence: Long) {
        callApi<MilkyRecallPrivateMessageRequest, MilkyApiEmptyResponse>(
            "recall_private_message",
            MilkyRecallPrivateMessageRequest(
                userId = userUin,
                messageSeq = sequence
            )
        )
    }

    override suspend fun recallGroupMessage(groupUin: Long, sequence: Long) {
        callApi<MilkyRecallGroupMessageRequest, MilkyApiEmptyResponse>(
            "recall_group_message",
            MilkyRecallGroupMessageRequest(
                groupId = groupUin,
                messageSeq = sequence
            )
        )
    }

    override suspend fun sendPrivateNudge(userUin: Long, isSelf: Boolean) {
        callApi<MilkySendFriendNudgeRequest, MilkyApiEmptyResponse>(
            "send_friend_nudge",
            MilkySendFriendNudgeRequest(
                userId = userUin,
                isSelf = isSelf
            )
        )
    }

    override suspend fun sendProfileLike(userUin: Long, count: Int) {
        callApi<MilkySendProfileLikeRequest, MilkyApiEmptyResponse>(
            "send_profile_like",
            MilkySendProfileLikeRequest(
                userId = userUin,
                count = count
            )
        )
    }

    override suspend fun getGroupAnnouncements(groupUin: Long): List<Announcement> {
        val group = getGroup(groupUin, cacheFirst = true)
            ?: throw MilkyException("Group with uin $groupUin not found")
        return callApi<MilkyGetGroupAnnouncementListRequest, MilkyGetGroupAnnouncementListResponse>(
            "get_group_announcement_list",
            MilkyGetGroupAnnouncementListRequest(
                groupId = groupUin
            )
        ).announcements.map { MilkyAnnouncement(group, it) }
    }

    override suspend fun setGroupName(groupUin: Long, name: String) {
        callApi<MilkySetGroupNameRequest, MilkyApiEmptyResponse>(
            "set_group_name",
            MilkySetGroupNameRequest(
                groupId = groupUin,
                name = name
            )
        )
    }

    override suspend fun setGroupAvatar(groupUin: Long, image: ResourceLocation) {
        callApi<MilkySetGroupAvatarRequest, MilkyApiEmptyResponse>(
            "set_group_avatar",
            MilkySetGroupAvatarRequest(
                groupId = groupUin,
                imageUri = image.toMilkyUri()
            )
        )
    }

    override suspend fun setGroupMemberCard(groupUin: Long, memberUin: Long, card: String) {
        callApi<MilkySetGroupMemberCardRequest, MilkyApiEmptyResponse>(
            "set_group_member_card",
            MilkySetGroupMemberCardRequest(
                groupId = groupUin,
                userId = memberUin,
                card = card
            )
        )
    }

    override suspend fun setGroupMemberSpecialTitle(
        groupUin: Long,
        memberUin: Long,
        title: String
    ) {
        callApi<MilkySetGroupMemberSpecialTitleRequest, MilkyApiEmptyResponse>(
            "set_group_member_special_title",
            MilkySetGroupMemberSpecialTitleRequest(
                groupId = groupUin,
                userId = memberUin,
                specialTitle = title
            )
        )
    }

    override suspend fun setGroupMemberAdmin(groupUin: Long, memberUin: Long, isPromote: Boolean) {
        callApi<MilkySetGroupMemberAdminRequest, MilkyApiEmptyResponse>(
            "set_group_member_admin",
            MilkySetGroupMemberAdminRequest(
                groupId = groupUin,
                userId = memberUin,
                isSet = isPromote
            )
        )
    }

    override suspend fun setGroupMemberMute(groupUin: Long, memberUin: Long, duration: Int) {
        callApi<MilkySetGroupMemberMuteRequest, MilkyApiEmptyResponse>(
            "set_group_member_mute",
            MilkySetGroupMemberMuteRequest(
                groupId = groupUin,
                userId = memberUin,
                duration = duration.toLong()
            )
        )
    }

    override suspend fun setGroupWholeMute(groupUin: Long, isMute: Boolean) {
        callApi<MilkySetGroupWholeMuteRequest, MilkyApiEmptyResponse>(
            "set_group_whole_mute",
            MilkySetGroupWholeMuteRequest(
                groupId = groupUin,
                isMute = isMute
            )
        )
    }

    override suspend fun kickGroupMember(groupUin: Long, memberUin: Long, isPermanent: Boolean) {
        callApi<MilkyKickGroupMemberRequest, MilkyApiEmptyResponse>(
            "kick_group_member",
            MilkyKickGroupMemberRequest(
                groupId = groupUin,
                userId = memberUin,
                rejectAddRequest = isPermanent
            )
        )
    }

    override suspend fun sendGroupAnnouncement(
        groupUin: Long,
        content: String,
        image: ResourceLocation?
    ) {
        callApi<MilkySendGroupAnnouncementRequest, MilkyApiEmptyResponse>(
            "send_group_announcement",
            MilkySendGroupAnnouncementRequest(
                groupId = groupUin,
                content = content,
                imageUri = image?.toMilkyUri()
            )
        )
    }

    override suspend fun deleteGroupAnnouncement(groupUin: Long, announcementId: String) {
        callApi<MilkyDeleteGroupAnnouncementRequest, MilkyApiEmptyResponse>(
            "delete_group_announcement",
            MilkyDeleteGroupAnnouncementRequest(
                groupId = groupUin,
                announcementId = announcementId
            )
        )
    }

    override suspend fun quitGroup(groupUin: Long) {
        callApi<MilkyQuitGroupRequest, MilkyApiEmptyResponse>(
            "quit_group",
            MilkyQuitGroupRequest(groupId = groupUin)
        )
    }

    override suspend fun sendGroupNudge(groupUin: Long, memberUin: Long) {
        callApi<MilkySendGroupNudgeRequest, MilkyApiEmptyResponse>(
            "send_group_nudge",
            MilkySendGroupNudgeRequest(
                groupId = groupUin,
                userId = memberUin
            )
        )
    }

    override suspend fun sendGroupMessageReaction(
        groupUin: Long,
        sequence: Long,
        reactionId: String,
        isAdd: Boolean
    ) {
        callApi<MilkySendGroupMessageReactionRequest, MilkyApiEmptyResponse>(
            "send_group_message_reaction",
            MilkySendGroupMessageReactionRequest(
                groupId = groupUin,
                messageSeq = sequence,
                reaction = reactionId,
                isAdd = isAdd
            )
        )
    }

    override suspend fun getRecentFriendRequests(limit: Int): List<FriendRequestEvent> =
        callApi<MilkyGetFriendRequestsRequest, MilkyGetFriendRequestsResponse>(
            "get_friend_requests",
            MilkyGetFriendRequestsRequest(limit)
        ).requests.map { it.toEvent(this) }

    override suspend fun getRecentGroupRequests(limit: Int): List<GroupRequestEvent> =
        callApi<MilkyGetGroupRequestsRequest, MilkyGetGroupRequestsResponse>(
            "get_group_requests",
            MilkyGetGroupRequestsRequest(limit)
        ).requests.map { it.toEvent(this) }

    override suspend fun getRecentGroupInvitations(limit: Int): List<GroupInvitationEvent> =
        callApi<MilkyGetGroupInvitationsRequest, MilkyGetGroupInvitationsResponse>(
            "get_group_invitations",
            MilkyGetGroupInvitationsRequest(limit)
        ).invitations.map { it.toEvent(this) }

    override suspend fun acceptRequest(requestId: String) {
        callApi<MilkyAcceptRequestRequest, MilkyApiEmptyResponse>(
            "accept_request",
            MilkyAcceptRequestRequest(requestId)
        )
    }

    override suspend fun rejectRequest(requestId: String, reason: String?) {
        callApi<MilkyRejectRequestRequest, MilkyApiEmptyResponse>(
            "reject_request",
            MilkyRejectRequestRequest(
                requestId = requestId,
                reason = reason ?: ""
            )
        )
    }

    override suspend fun uploadPrivateFile(userUin: Long, file: ResourceLocation, fileName: String): String =
        callApi<MilkyUploadPrivateFileRequest, MilkyUploadPrivateFileResponse>(
            "upload_private_file",
            MilkyUploadPrivateFileRequest(
                userId = userUin,
                fileUri = file.toMilkyUri(),
                fileName = fileName
            )
        ).fileId

    override suspend fun getPrivateFileDownloadUrl(userUin: Long, fileId: String): String =
        callApi<MilkyGetPrivateFileDownloadUrlRequest, MilkyGetPrivateFileDownloadUrlResponse>(
            "get_private_file_download_url",
            MilkyGetPrivateFileDownloadUrlRequest(
                userId = userUin,
                fileId = fileId
            )
        ).downloadUrl

    override suspend fun uploadGroupFile(
        groupUin: Long,
        file: ResourceLocation,
        fileName: String,
        parentFolderId: String
    ): String =
        callApi<MilkyUploadGroupFileRequest, MilkyUploadGroupFileResponse>(
            "upload_group_file",
            MilkyUploadGroupFileRequest(
                groupId = groupUin,
                fileUri = file.toMilkyUri(),
                fileName = fileName,
                targetFolderId = parentFolderId
            )
        ).fileId

    override suspend fun getGroupFiles(
        groupUin: Long,
        parentFolderId: String
    ): List<FileSystemEntry> {
        val group = getGroup(groupUin, cacheFirst = true)
            ?: throw MilkyException("Group with uin $groupUin not found")
        val response = callApi<MilkyGetGroupFilesRequest, MilkyGetGroupFilesResponse>(
            "get_group_files",
            MilkyGetGroupFilesRequest(
                groupId = groupUin,
                parentFolderId = parentFolderId
            )
        )
        return response.folders.map { MilkyFolderEntry(this, group, it) } +
                response.files.map { MilkyFileEntry(this, group, it) }
    }

    override suspend fun getGroupFileDownloadUrl(groupUin: Long, fileId: String): String =
        callApi<MilkyGetGroupFileDownloadUrlRequest, MilkyGetGroupFileDownloadUrlResponse>(
            "get_group_file_download_url",
            MilkyGetGroupFileDownloadUrlRequest(
                groupId = groupUin,
                fileId = fileId
            )
        ).downloadUrl

    override suspend fun moveGroupFile(
        groupUin: Long,
        fileId: String,
        fromFolderId: String,
        targetFolderId: String
    ) {
        callApi<MilkyMoveGroupFileRequest, MilkyApiEmptyResponse>(
            "move_group_file",
            MilkyMoveGroupFileRequest(
                groupId = groupUin,
                fileId = fileId,
                targetFolderId = targetFolderId
            )
        )
    }

    override suspend fun renameGroupFile(
        groupUin: Long,
        fileId: String,
        newName: String
    ) {
        callApi<MilkyRenameGroupFileRequest, MilkyApiEmptyResponse>(
            "rename_group_file",
            MilkyRenameGroupFileRequest(
                groupId = groupUin,
                fileId = fileId,
                newName = newName
            )
        )
    }

    override suspend fun deleteGroupFile(groupUin: Long, fileId: String) {
        callApi<MilkyDeleteGroupFileRequest, MilkyApiEmptyResponse>(
            "delete_group_file",
            MilkyDeleteGroupFileRequest(
                groupId = groupUin,
                fileId = fileId
            )
        )
    }

    override suspend fun createGroupFolder(groupUin: Long, folderName: String): String =
        callApi<MilkyCreateGroupFolderRequest, MilkyCreateGroupFolderResponse>(
            "create_group_folder",
            MilkyCreateGroupFolderRequest(
                groupId = groupUin,
                folderName = folderName
            )
        ).folderId

    override suspend fun renameGroupFolder(
        groupUin: Long,
        folderId: String,
        newName: String
    ) {
        callApi<MilkyRenameGroupFolderRequest, MilkyApiEmptyResponse>(
            "rename_group_folder",
            MilkyRenameGroupFolderRequest(
                groupId = groupUin,
                folderId = folderId,
                newName = newName
            )
        )
    }

    override suspend fun deleteGroupFolder(groupUin: Long, folderId: String) {
        callApi<MilkyDeleteGroupFolderRequest, MilkyApiEmptyResponse>(
            "delete_group_folder",
            MilkyDeleteGroupFolderRequest(
                groupId = groupUin,
                folderId = folderId
            )
        )
    }
}