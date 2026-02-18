package org.ntqqrev.milky

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ClientTest {
    @Test
    fun test() = runBlocking {
        val client = MilkyClient {
            addressBase = "http://localhost:3000"
            eventConnectionType = EventConnectionType.WebSocket
        }

        val loginInfo = client.getLoginInfo()
        println("Login uin: ${loginInfo.uin}")

        val userProfile = client.getUserProfile(loginInfo.uin)
        println("Your nickname: ${userProfile.nickname}")
        println("Your bio: ${userProfile.bio}")

        client.connectEvent()
        val job = launch {
            client.eventFlow
                .filterIsInstance<Event.MessageReceive>()
                .collect { event ->
                    when (val data = event.data) {
                        is IncomingMessage.Group -> {
                            println("Group message from ${data.senderId} in ${data.group.groupId}:")
                            println(milkyJsonModule.encodeToString(data.segments))
                        }
                        is IncomingMessage.Friend -> {
                            println("Private message from ${data.senderId}:")
                            println(milkyJsonModule.encodeToString(data.segments))
                        }
                        else -> {}
                    }
                }
        }
        delay(30000L)
        job.cancel()
        client.disconnectEvent()
    }
}