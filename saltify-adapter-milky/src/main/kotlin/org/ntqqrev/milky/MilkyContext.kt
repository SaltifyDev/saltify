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
import org.ntqqrev.milky.entity.MilkyFriend
import org.ntqqrev.milky.entity.MilkyGroup
import org.ntqqrev.milky.entity.MilkyGroupMember
import org.ntqqrev.milky.exception.MilkyApiNotFoundException
import org.ntqqrev.milky.exception.MilkyBadCredentialsException
import org.ntqqrev.milky.exception.MilkyException
import org.ntqqrev.milky.message.*
import org.ntqqrev.milky.protocol.api.*
import org.ntqqrev.milky.protocol.event.*
import org.ntqqrev.milky.protocol.message.MilkyFriendMessageData
import org.ntqqrev.milky.protocol.message.MilkyGroupMessageData
import org.ntqqrev.milky.protocol.message.MilkyIncomingMessageData
import org.ntqqrev.milky.protocol.request.*
import org.ntqqrev.milky.util.toMilkyMessageScene
import org.ntqqrev.milky.util.toSaltifyMessageScene
import org.ntqqrev.milky.util.toSaltifyRequestState
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.event.*
import org.ntqqrev.saltify.getMember
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.ForwardedIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.MessageSendResult
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.message.outgoing.ResourceLocation
import org.ntqqrev.saltify.model.group.Announcement
import org.ntqqrev.saltify.model.group.FileEntry
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

    private val base = if (init.milkyUrl.endsWith("/"))
        init.milkyUrl
    else
        "${init.milkyUrl}/"

    private val wsUrl = if (init.milkyAccessToken.isEmpty())
        "${base}event"
    else
        "${base}event?access_token=${init.milkyAccessToken}"

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = JacksonWebsocketContentConverter(objectMapper)
        }
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }
    }

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
        val response = client.post("${base}api/$name") {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                if (init.milkyAccessToken.isNotEmpty()) {
                    append("Authorization", "Bearer ${init.milkyAccessToken}")
                }
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
        val message = when (this) {
            is MilkyFriendMessageData ->
                MilkyIncomingPrivateMessage.fromFriendMessage(this@MilkyContext, this)

            is MilkyGroupMessageData ->
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

    override suspend fun getLoginInfo(): Pair<Long, String> = callApi<MilkyApiEmptyRequest, MilkyGetLoginInfoResponse>(
        "get_login_info",
        MilkyApiEmptyRequest()
    ).let { Pair(it.uin, it.nickname) }

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
        TODO("Not yet implemented")
    }

    override suspend fun sendProfileLike(userUin: Long, count: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupAnnouncements(groupUin: Long): List<Announcement> {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupName(groupUin: Long, name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupAvatar(groupUin: Long, image: ResourceLocation) {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupMemberCard(groupUin: Long, memberUin: Long, card: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupMemberSpecialTitle(
        groupUin: Long,
        memberUin: Long,
        title: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupMemberAdmin(groupUin: Long, memberUin: Long, isPromote: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupMemberMute(groupUin: Long, memberUin: Long, duration: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupWholeMute(groupUin: Long, isMute: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun kickGroupMember(groupUin: Long, memberUin: Long, isPermanent: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun sendGroupAnnouncement(
        groupUin: Long,
        content: String,
        image: ResourceLocation?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroupAnnouncement(groupUin: Long, announcementId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun quitGroup(groupUin: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun sendGroupNudge(groupUin: Long, memberUin: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun sendGroupMessageReaction(
        groupUin: Long,
        sequence: Long,
        reactionId: String,
        isAdd: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentFriendRequests(limit: Int): List<FriendRequestEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentGroupRequests(limit: Int): List<GroupRequestEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentGroupInvitations(limit: Int): List<GroupInvitationEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun acceptRequest(requestId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun rejectRequest(requestId: String, reason: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadPrivateFile(userUin: Long, file: ResourceLocation): String {
        TODO("Not yet implemented")
    }

    override suspend fun getPrivateFileDownloadUrl(userUin: Long, fileId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun uploadGroupFile(
        groupUin: Long,
        file: ResourceLocation,
        parentFolderId: String
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupFiles(
        groupUin: Long,
        parentFolderId: String
    ): List<FileSystemEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupFileDownloadUrl(groupUin: Long, fileId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun moveGroupFile(
        groupUin: Long,
        fileId: String,
        fromFolderId: String,
        targetFolderId: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun renameGroupFile(
        groupUin: Long,
        fileId: String,
        newName: String
    ): FileEntry {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroupFile(groupUin: Long, fileId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun createGroupFolder(groupUin: Long, folderName: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun renameGroupFolder(
        groupUin: Long,
        folderId: String,
        newName: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroupFolder(groupUin: Long, folderId: String) {
        TODO("Not yet implemented")
    }
}