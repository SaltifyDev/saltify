package org.ntqqrev.lagrange

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.ntqqrev.lagrange.exception.LagrangeException
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.service.system.BotOnline
import org.ntqqrev.lagrange.internal.service.system.FetchQrCode
import org.ntqqrev.lagrange.internal.service.system.QueryQrCodeState
import org.ntqqrev.lagrange.internal.service.system.WtLogin
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.Context
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
import org.ntqqrev.saltify.message.outgoing.ResourceLocation
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.group.Announcement
import org.ntqqrev.saltify.model.group.FileSystemEntry
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.properties.Delegates

class LagrangeContext internal constructor(
    internal val client: LagrangeClient,
    internal val init: LagrangeInit,
    internal val env: Environment,
    internal val flow: MutableSharedFlow<Event>,
) : Context {
    private val logger = KotlinLogging.logger { }
    internal val objectMapper = jacksonObjectMapper()

    private var instanceState by Delegates.observable(Context.State.INITIALIZED) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            env.scope.launch {
                flow.emit(
                    ContextStateChangeEvent(
                        this@LagrangeContext,
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

    internal suspend fun qrCodeLogin() {
        logger.info { "Session is empty, using QR code login" }

        client.sessionStore.clear()

        val fetchQrCodeResult = client.callService(FetchQrCode)
        logger.info { "QR code URL: ${fetchQrCodeResult.qrCodeUrl}" }
        env.scope.launch {
            env.rootDataPath.resolve(qrCodeFileName).writeBytes(fetchQrCodeResult.qrCodePng)
            logger.info { "QR code saved to data/qrcode.png" }
        }

        while (true) {
            val qrCodeState = client.callService(QueryQrCodeState)
            logger.debug { "QrCodeState: ${QueryQrCodeState.Result.getString(qrCodeState)}" }
            if (qrCodeState.value == QueryQrCodeState.Result.Confirmed.value) {
                if (client.sessionStore.uin != init.uin) {
                    client.packetLogic.disconnect()
                    instanceState = Context.State.TERMINATED
                    throw LagrangeException("Uin mismatch: expected ${init.uin}, got ${client.sessionStore.uin}")
                }
                break
            }
            delay(3000)
        }
        logger.info { "QR code has been confirmed" }

        val isLoginSuccess = client.callService(WtLogin)
        if (!isLoginSuccess) {
            instanceState = Context.State.TERMINATED
            throw RuntimeException("QR code login failed")
        }

        logger.info { "Credentials retrieved, trying online" }
        env.scope.launch {
            env.rootDataPath.resolve(sessionStoreFileName)
                .writeText(objectMapper.writeValueAsString(client.sessionStore))
        }

        val onlineResult = client.callService(BotOnline)
        if (!onlineResult) {
            instanceState = Context.State.TERMINATED
            throw RuntimeException("Login failed")
        }
    }

    internal suspend fun fastLogin() {
        logger.info { "Login with existing session" }
        val onlineResult = client.callService(BotOnline)
        if (!onlineResult) {
            instanceState = Context.State.TERMINATED
            throw RuntimeException("Login failed")
            // TODO: key exchange if session is expired
        }
    }

    override suspend fun start() {
        if (client.sessionStore.d2.isEmpty())
            qrCodeLogin()
        else
            fastLogin()

        logger.info { "Logging in success" }
        instanceState = Context.State.STARTED
        // TODO: collect internal events
    }

    override suspend fun stop() {
        logger.info { "Logging out" }
        // TODO: send logout packet
        client.packetLogic.disconnect()
        instanceState = Context.State.TERMINATED
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

    override suspend fun getGroup(
        groupUin: Long,
        cacheFirst: Boolean
    ): Group? {
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
    ): IncomingMessage {
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

    override suspend fun recallPrivateMessage(userUin: Long, sequence: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun recallGroupMessage(groupUin: Long, sequence: Long) {
        TODO("Not yet implemented")
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

    override suspend fun uploadPrivateFile(userUin: Long, file: ResourceLocation, fileName: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun getPrivateFileDownloadUrl(userUin: Long, fileId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun uploadGroupFile(
        groupUin: Long,
        file: ResourceLocation,
        fileName: String,
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
    ) {
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