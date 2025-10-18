package org.ntqqrev.milky

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.log
import kotlin.test.Test

class ClientTest {
    @Test
    fun test() = runBlocking {
        val client = MilkyClient(
            addressBase = "http://localhost:3000",
            eventConnectionType = EventConnectionType.WebSocket
        )

        val loginInfo = client.callApi(ApiEndpoint.GetLoginInfo)
        println("Login uin: ${loginInfo.uin}")

        val userProfile = client.callApi(
            ApiEndpoint.GetUserProfile,
            GetUserProfileInput(userId = loginInfo.uin)
        )
        println("Your nickname: ${userProfile.nickname}")
        println("Your bio: ${userProfile.bio}")

        client.connectEvent()
        val job = launch {
            client.subscribe {
                if (it is Event.MessageReceive) {
                    when (it.data) {
                        is IncomingMessage.Group -> {
                            println("Group message from ${it.data.peerId} by ${it.data.senderId}:")
                            println(milkyJsonModule.encodeToString(it.data.segments))
                        }

                        else -> {}
                    }
                }
            }
        }
        delay(30000L)
        job.cancel()
        client.disconnectEvent()
    }
}