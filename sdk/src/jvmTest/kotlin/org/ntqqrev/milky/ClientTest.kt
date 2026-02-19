package org.ntqqrev.milky

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ntqqrev.milky.core.MilkyClient
import org.ntqqrev.milky.core.getLoginInfo
import org.ntqqrev.milky.core.getUserProfile
import org.ntqqrev.milky.core.text
import org.ntqqrev.milky.dsl.milkyPlugin
import org.ntqqrev.milky.entity.EventConnectionType
import kotlin.test.Test

class ClientTest {
    @Test
    fun test() = runBlocking {
        val client = MilkyClient {
            addressBase = "http://localhost:3000"
            eventConnectionType = EventConnectionType.WebSocket

            install(mainPlugin)
        }

        client.connectEvent()
        delay(30000L)
        client.disconnectEvent()
        client.close()
    }

    val mainPlugin = milkyPlugin("main") {
        onStart {
            val loginInfo = client.getLoginInfo()
            println("Login uin: ${loginInfo.uin}")

            val userProfile = client.getUserProfile(loginInfo.uin)
            println("Your nickname: ${userProfile.nickname}")
            println("Your bio: ${userProfile.bio}")
        }

        on<Event.MessageReceive> {
            when (val data = it.data) {
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

        command("hello milky", prefix = "") { event ->
            event.reply {
                text("Hello milky!")
            }
        }
    }
}
