package org.ntqqrev.saltify

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.milkyJsonModule
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.getLoginInfo
import org.ntqqrev.saltify.core.text
import org.ntqqrev.saltify.dsl.createSaltifyPlugin
import org.ntqqrev.saltify.dsl.parameter
import org.ntqqrev.saltify.entity.EventConnectionType
import org.ntqqrev.saltify.entity.SaltifyComponentType
import org.ntqqrev.saltify.util.coroutine.saltifyComponent
import kotlin.test.Test

class PluginTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = SaltifyApplication {
            addressBase = "http://localhost:3000"
            eventConnectionType = EventConnectionType.WebSocket

            install(testPlugin)
        }

        launch {
            client.exceptionFlow.collect { (context, exception) ->
                val component = context.saltifyComponent!!

                when (component.type) {
                    SaltifyComponentType.Application -> throw exception
                    else -> println(
                        "Component ${component.name}(${component.type}) occurred an exception: " +
                            exception.stackTraceToString()
                    )
                }
            }
        }

        client.connectEvent()
        delay(30000L)
        client.disconnectEvent()
        client.close()
    }

    val testPlugin = createSaltifyPlugin("test") {
        onStart {
            println("--- Test plugin started")
            val self = client.getLoginInfo()
            println("Current uin：${self.uin}")
            error("test exception")
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
                    text("Command run failed: $it")
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
                        text("Command run failed: $it")
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
