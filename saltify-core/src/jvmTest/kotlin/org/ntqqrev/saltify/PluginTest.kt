package org.ntqqrev.saltify

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.saltify.builtin.plugin.commandHelp
import org.ntqqrev.saltify.builtin.plugin.defaultLogging
import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.extension.*
import org.ntqqrev.saltify.model.event.EventConnectionType
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

class PluginTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = SaltifyApplication {
            connection {
                baseUrl = "http://***REMOVED***"
                accessToken = "***REMOVED***"

                events {
                    type = EventConnectionType.WebSocket
                    autoReconnect = false
                }
            }

            bot {
                superUsers = mutableSetOf(3650502250, 3521766148)
            }

            install(testPlugin) {
                response = "Hello!!"
            }

            install(commandHelp)
            install(defaultLogging)
        }.start()

        client.connectEvent()
        delay(60000.milliseconds)
        client.disconnectEvent()
        client.close()
    }

    class TestConfig(var response: String = "Hello!")

    val testPlugin = SaltifyPlugin(config = ::TestConfig) { config ->
        onStart {
            val self = client.getLoginInfo()
            logger.info("当前登录QQ：${self.uin}")
            logger.info("测试插件加载成功")
        }

        onStop {
            logger.info("测试插件已停止")
        }

        // regex test
        regex("""BV1\w{9}""") { matches ->
            respond(matches.joinToString { it.value })
        }

        // config test
        command("hello") {
            onExecute {
                logger.info("config: ${config.response}")
                respond(config.response)
            }
        }

        // greedy test
        command("say") {
            val content = parameter.greedyString("content", "words to repeat")

            onExecute {
                respond(content.value)
            }

            onFailure {
                respond("Command run failed: ${it.message}")
            }
        }

        // error handling test
        command("error") {
            onExecute {
                error("test exception")
            }
        }

        // context propagation test
        command("shutdown") {
            onExecute {
                respond("Are you sure? (yes/no)")

                val event = awaitNextMessage()

                if (event == null) {
                    respond("Operation cancelled due to timeout")
                } else {
                    val content = event.segments.plainText
                    respond("You just responded \"$content\". However, whatever you say I won't shutdown myself.")
                }
            }
        }

        // sub command test
        command("math") {
            // /math add <num1> <num2>
            subCommand("add") {
                val a = parameter.int("a")
                val b = parameter.int("b")

                onExecute {
                    val result = a.value + b.value
                    respond("$result")
                }

                onFailure {
                    respond("Command run failed: ${it.message}")
                }
            }

            // /math power <base>
            subCommand("power") {
                val base = parameter.int("base")

                onExecute {
                    val value = base.value
                    respond("The power of $value is ${value * value}")
                }
            }
        }

        // chat environment test
        command("whereami") {
            onGroupExecute {
                val data = event.data as IncomingMessage.Group
                respond("In group：${data.group.groupName} (${data.group.groupId})")
            }

            onPrivateExecute {
                respond("In private chat")
            }

            onExecute {
                respond("Unknown chat environment")
            }
        }

        // comprehensive parameters test
        // /order <id> <note>
        command("order") {
            val id = parameter.int("id")
            val note = parameter.greedyString("note")

            onExecute {
                respond("Order #${id.value} created\nnote：${note.value}")
            }
        }
    }
}
