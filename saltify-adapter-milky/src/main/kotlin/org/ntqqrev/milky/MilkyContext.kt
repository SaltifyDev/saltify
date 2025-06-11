package org.ntqqrev.milky

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.serialization.jackson.JacksonWebsocketContentConverter
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.ntqqrev.milky.exception.MilkyApiNotFoundException
import org.ntqqrev.milky.exception.MilkyBadCredentialsException
import org.ntqqrev.milky.exception.MilkyException
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.event.ContextStateChangeEvent
import org.ntqqrev.saltify.event.Event
import org.ntqqrev.saltify.event.FriendRequestEvent
import org.ntqqrev.saltify.event.GroupInvitationEvent
import org.ntqqrev.saltify.event.GroupRequestEvent
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.ForwardedIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.MessageSendResult
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.group.Announcement
import org.ntqqrev.saltify.model.group.FileEntry
import org.ntqqrev.saltify.model.group.FileSystemEntry
import java.io.InputStream
import kotlin.properties.Delegates

class MilkyContext internal constructor(
    val init: MilkyInit,
    val env: Environment,
    val channel: MutableSharedFlow<Event>,
) : Context {
    private val logger = KotlinLogging.logger { }

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
            val objectMapper = jacksonObjectMapper()
            objectMapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            contentConverter = JacksonWebsocketContentConverter(objectMapper)
        }
        install(ContentNegotiation) {
            jackson {
                propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            }
        }
    }

    private var instanceState by Delegates.observable(Context.State.INITIALIZED) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            env.scope.launch {
                channel.emit(
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

    override suspend fun start() {
        while (env.scope.isActive) {
            try {
                client.webSocket(wsUrl) {
                    logger.info { "Connected to $wsUrl" }
                    instanceState = Context.State.STARTED
                    // TODO: pipe events to channel
                }
                logger.error { "WebSocket connection lost due to server" }
            } catch (e: Exception) {
                logger.error(e) { "WebSocket connection lost due to exception" }
            }
            instanceState = Context.State.INTERRUPTED
            delay(init.wsReconnectInterval)
            logger.info { "Reconnecting to $wsUrl..." }
        }
    }

    private suspend inline fun <reified T, reified R> callApi(name: String, body: T): R {
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
        return response.body<R>()
    }

    override suspend fun stop() {
        client.close()
        instanceState = Context.State.STOPPED
    }

    override suspend fun getLoginInfo(): Pair<Long, String> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllFriends(cacheFirst: Boolean): Iterable<Friend> {
        TODO("Not yet implemented")
    }

    override suspend fun getFriend(
        friendUin: Long,
        cacheFirst: Boolean
    ): Friend? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGroups(cacheFirst: Boolean): Iterable<Group> {
        TODO("Not yet implemented")
    }

    override suspend fun getGroup(groupUin: Long, cacheFirst: Boolean): Group? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGroupMembers(
        groupUin: Long,
        cacheFirst: Boolean
    ): Iterable<GroupMember> {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupMember(
        groupUin: Long,
        memberUin: Long,
        cacheFirst: Boolean
    ): GroupMember? {
        TODO("Not yet implemented")
    }

    override suspend fun sendPrivateMessage(
        userUin: Long,
        builder: PrivateMessageBuilder.() -> Unit
    ): MessageSendResult {
        TODO("Not yet implemented")
    }

    override suspend fun sendGroupMessage(
        groupUin: Long,
        builder: GroupMessageBuilder.() -> Unit
    ): MessageSendResult {
        TODO("Not yet implemented")
    }

    override suspend fun getMessage(
        messageScene: MessageScene,
        peerId: Long,
        sequence: Long
    ): IncomingMessage? {
        TODO("Not yet implemented")
    }

    override suspend fun getHistoryMessages(
        messageScene: MessageScene,
        peerId: Long,
        startSequence: Long?,
        isBackward: Boolean,
        limit: Int
    ): List<IncomingMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun getResourceTempUrl(resourceId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun getForwardedMessages(forwardId: String): List<ForwardedIncomingMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun recallPrivateMessage(
        userUin: Long,
        sequence: Long,
        clientSequence: Long
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun recallGroupMessage(groupUin: Long, sequence: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun sendPrivateNudge(userUin: Long, isSelf: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun sendProfileLike(userUin: Long, count: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupAnnouncements(groupUin: Long): List<Announcement> {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupName(groupUin: Long, name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupAvatar(groupUin: Long, image: InputStream) {
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
        image: InputStream?
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

    override suspend fun uploadPrivateFile(userUin: Long, file: InputStream): String {
        TODO("Not yet implemented")
    }

    override suspend fun getPrivateFileDownloadUrl(
        userUin: Long,
        fileId: String,
        fileHash: String
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun uploadGroupFile(
        groupUin: Long,
        file: InputStream,
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

    override suspend fun deleteGroupFile(groupUin: Long, fileId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun createGroupFolder(groupUin: Long, folderName: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun renameGroupFolder(
        groupUin: Long,
        folderId: String,
        newName: String
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroupFolder(groupUin: Long, folderId: String): Boolean {
        TODO("Not yet implemented")
    }
}