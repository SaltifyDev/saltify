import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.ntqqrev.milky.MilkyContextFactory
import org.ntqqrev.milky.MilkyInit
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.message.MessageScene


suspend fun main() {
    testEnv.scope.run {
        val ctx = MilkyContextFactory.createContext(
            init = MilkyInit(milkyUrl = "127.0.0.1:3000"),
            env = testEnv,
            flow = MutableSharedFlow()
        )

        launch { ctx.start() }

        val tests = listOf(
            NamedTest("Send_Private_Message", ::testSendPrivateMessage),
            NamedTest("Send_Group_Message", ::testSendGroupMessage),
            // NamedTest("Get_Message", ::testGetMessage), TODO: When Lagrange.Milky supports `getHistoryMessages()`
            // NamedTest("Get_History_Messages", ::testGetHistoryMessages)
        )
        for (test in tests) {
            runTest(test, ctx)
        }
    }
}

private suspend fun testSendPrivateMessage(ctx: Context) {
    val (userUin, _) = ctx.getLoginInfo()
    val result = ctx.sendPrivateMessage(userUin) {
        text("Hello, World!")
    }
    logger.info { "Private message sent: $result" }
}

private suspend fun testSendGroupMessage(ctx: Context) {
    val group = ctx.getAllGroups().toList().random()
    val result = ctx.sendGroupMessage(group.uin) {
        text("Hello, World!")
    }
    logger.info { "Group message sent: $result" }
}

private suspend fun testGetMessage(ctx: Context) {
    val group = ctx.getAllGroups().toList().random()
    val messageSeq = ctx.getHistoryMessages(MessageScene.GROUP, group.uin).random().sequence
    val msg = ctx.getMessage(MessageScene.GROUP, group.uin, messageSeq)
    msg.segments.forEach { logger.info { it.toString() } }
}

private suspend fun testGetHistoryMessages(ctx: Context) {
    val group = ctx.getAllGroups().toList().random()
    val messages = ctx.getHistoryMessages(MessageScene.GROUP, group.uin)
    messages.forEach { logger.info { it.toString() } }
}