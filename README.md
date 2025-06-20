<div align="center">

<h1>Saltify</h1>

跨平台 & 可扩展的 QQ Bot 框架

[QQ 群](https://qm.qq.com/q/C04kPQzayk) | [Telegram](https://t.me/WeavingStar)

</div>

## 快速开始

```kotlin
class Config {
    val echoPrefix = "::"
}

val plugin = plugin<Config> {
    onStart {
        println("Plugin started")
        // do something with side effects
    }

    command("echo") {
        // define command parameters in order
        val pMessage = parameter<String>("message", "The message to echo")
        onExecute {
            val message = capture(pMessage)
            respond {
                image(remote("https://example.com/image.png"))
                text("${config.echoPrefix} $message")
            }
        }
    }
    
    on<GroupJoinRequestEvent> {
        val group = ctx.getGroup(it.groupUin)
        group?.sendMessage("${it.initiatorUin} requested to join group with ${it.comment}")
    }
    
    onStop { 
        // do something to clear side effects
    }
}
```