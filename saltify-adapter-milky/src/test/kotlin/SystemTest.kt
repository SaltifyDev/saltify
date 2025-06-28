import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.ntqqrev.milky.MilkyContextFactory
import org.ntqqrev.milky.MilkyInit
import org.ntqqrev.saltify.Context
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

suspend fun main() {
    testEnv.scope.run {
        val ctx = MilkyContextFactory.createContext(
            init = MilkyInit(milkyUrl = "127.0.0.1:3000"),
            env = testEnv,
            flow = MutableSharedFlow()
        )

        launch {
            try {
                ctx.start()
            } catch (e: Exception) {
                logger.error(e) { "❌ Context start failed" }
            }
        }

        listOf<suspend (Context) -> Unit>(
            ::testGetLoginInfo,
            ::testGetFriendList,
            ::testGetFriendInfo,
            ::testGetGroupList,
            ::testGetGroupInfo,
            ::testGetGroupMemberList,
            ::testGetGroupMemberInfo,
            ::testGetImplInfo
        ).forEach { test ->
            runTest(test, ctx)
        }

    }
}

private suspend fun runTest(testFn: suspend (Context) -> Unit, ctx: Context) {
    val name = testFn.javaClass.name.substringAfter("test")
    logger.info { "🔍 Starting test: $name" }
    val time = measureTimeMillis {
        try {
            testFn(ctx)
            logger.info { "✅ Test $name succeeded" }
        } catch (e: Exception) {
            logger.error(e) { "❌ Test $name failed" }
        }
    }
    logger.info { "⏱️ Test $name finished in ${time}ms\n" }
}

private suspend fun testGetLoginInfo(ctx: Context) {
    ctx.getLoginInfo().toList().forEach {
        logger.info { "Login info: $it" }
    }
}

private suspend fun testGetFriendList(ctx: Context) {
    val friends = ctx.getAllFriends().toList()
    friends.forEach { logger.info { "Friend: ${it.nickname}" } }
}

private suspend fun testGetFriendInfo(ctx: Context) {
    val friends = ctx.getAllFriends().toList()
    val friend = ctx.getFriend(friends.random().uin)
    logger.info { "Friend info: ${friend?.nickname}" }
}

private suspend fun testGetGroupList(ctx: Context) {
    ctx.getAllGroups().toList().forEach {
        logger.info { "Group: ${it.name}" }
    }
}

private suspend fun testGetGroupInfo(ctx: Context) {
    val groups = ctx.getAllGroups().toList()
    val group = ctx.getGroup(groups.random().uin)
    logger.info { "Group info: ${group?.name}, ${group?.uin}, ${group?.memberCount}" }
}

private suspend fun testGetGroupMemberList(ctx: Context) {
    val group = ctx.getAllGroups().toList().random()
    val members = ctx.getAllGroupMembers(group.uin).toList()
    members.forEach { logger.info { "Member of ${group.name}: ${it.nickname}" } }
}

private suspend fun testGetGroupMemberInfo(ctx: Context) {
    val group = ctx.getAllGroups().toList().random()
    val members = ctx.getAllGroupMembers(group.uin).toList()
    val member = ctx.getGroupMember(group.uin, members.random().uin)
    logger.info { "Group member info (${group.name}): ${member?.nickname}" }
}

private suspend fun testGetImplInfo(ctx: Context) { /*TODO*/ }