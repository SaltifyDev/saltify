package org.ntqqrev.saltify

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.getLoginInfo
import org.ntqqrev.saltify.core.text
import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.extension.parameter
import org.ntqqrev.saltify.extension.plainText
import org.ntqqrev.saltify.model.EventConnectionType
import kotlin.test.Test

class PluginTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = SaltifyApplication {
            connection {
                baseUrl = "http://localhost:3000"
                events {
                    type = EventConnectionType.WebSocket
                    autoReconnect = false
                }
            }

            install(testPlugin) {
                response = "Hello!!"
            }
        }.start()

        client.connectEvent()
        delay(60000L)
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
        regex("""BV1\w{9}""") { event, matches ->
            event.respond {
                text(matches.joinToString { it.value })
            }
        }

        // config test
        command("hello") {
            onExecute {
                logger.info("config: ${config.response}")
                respond { text(config.response) }
            }
        }

        // greedy test
        command("say") {
            val content = greedyStringParameter("content", "words to repeat")

            onExecute {
                respond {
                    text(content.value)
                }
            }

            onFailure {
                respond {
                    text("Command run failed: ${it.message}")
                }
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
                respond {
                    text("Are you sure? (yes/no)")
                }

                val event = awaitNextMessage()

                if (event == null) {
                    respond { text("Operation cancelled due to timeout") }
                } else {
                    val content = event.segments.plainText
                    respond {
                        text("You just responded \"$content\". However, whatever you say I won't shutdown myself.")
                    }
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
