import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.ntqqrev.milky.MilkyContextFactory
import org.ntqqrev.milky.MilkyInit
import org.ntqqrev.saltify.Context

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

        val tests = listOf(
            NamedTest("Get_Login_Info", ::testGetLoginInfo),
            NamedTest("Get_Friend_List", ::testGetFriendList),
            NamedTest("Get_Friend_Info", ::testGetFriendInfo),
            NamedTest("Get_Group_List", ::testGetGroupList),
            NamedTest("Get_Group_Info", ::testGetGroupInfo),
            NamedTest("Get_GroupMember_List", ::testGetGroupMemberList),
            NamedTest("Get_GroupMember_Info", ::testGetGroupMemberInfo),
            NamedTest("Get_Impl_Info", ::testGetImplInfo),
        )

        for (test in tests) {
            runTest(test, ctx)
        }

    }
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

private suspend fun testGetImplInfo(ctx: Context) {
    // TODO: Not yet implemented
    logger.info { "TODO: Not yet implemented" }
}