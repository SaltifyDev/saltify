package org.ntqqrev.milky

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ntqqrev.milky.core.MilkyClient
import org.ntqqrev.milky.core.getLoginInfo
import org.ntqqrev.milky.core.text
import org.ntqqrev.milky.dsl.milkyPlugin
import org.ntqqrev.milky.dsl.parameter
import org.ntqqrev.milky.entity.EventConnectionType
import org.ntqqrev.milky.extension.on
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test

class ClientTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = MilkyClient {
            addressBase = "http://localhost:3000"
            eventConnectionType = EventConnectionType.WebSocket

            install(testPlugin)
        }

        client.on<IllegalStateException> { _, e ->
            println("Receive exception: ${e.message}")
            if (e.message != "This should not fail the test") {
                this@runBlocking.cancel(CancellationException(e.message, e))
            }
        }

        client.connectEvent()
        delay(30000L)
        client.disconnectEvent()
        client.close()
    }

    val testPlugin = milkyPlugin("test") {
        onStart {
            println("--- Test plugin started")
            val self = client.getLoginInfo()
            println("Current uin：${self.uin}")
            error("This should not fail the test")
        }

        onStop {
            println("--- Test plugin stopped")
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

        command("error") {
            onExecute {
                error("This should fail the test")
            }
        }

        // greedy test
        command("say") {
            val content = greedyStringParameter("content", "words to repeat")

            onExecute {
                val text = content.value
                respond {
                    text(text)
                }
            }

            onFailure {
                respond {
                    text("Command run failed: ${it.message}")
                }
            }
        }

        // sub command test
        command("math") {
            // /math add <num1> <num2>
            subCommand("add") {
                val a = parameter<Int>("a")
                val b = parameter<Int>("b")

                onExecute {
                    val result = a.value + b.value
                    respond { text("$result") }
                }

                onFailure {
                    respond {
                        text("Command run failed: ${it.message}")
                    }
                }
            }

            // /math power <base>
            subCommand("power") {
                val base = parameter<Int>("base")
                onExecute {
                    val value = base.value
                    respond { text("The power of $value is ${value * value}") }
                }
            }
        }

        // chat environment test
        command("whereami") {
            onGroupExecute {
                val data = event.data as IncomingMessage.Group
                respond {
                    text("In group：${data.group.groupName} (${data.group.groupId})")
                }
            }

            onPrivateExecute {
                respond {
                    text("In private chat")
                }
            }

            onExecute {
                respond { text("unknown") }
            }
        }

        // comprehensive parameters test
        // /order <id> <note>
        command("order") {
            val id = parameter<Int>("id")
            val note = greedyStringParameter("note")

            onExecute {
                respond {
                    text("Order #${id.value} created\nnote：${note.value}")
                }
            }
        }
    }
}
