<div align="center">

<h1>Saltify</h1>

跨平台 & 可扩展的 QQ Bot 框架

[QQ 群](https://qm.qq.com/q/C04kPQzayk) | [Telegram](https://t.me/WeavingStar)

</div>

## 模块说明

- [**saltify**](./saltify) - 框架核心模块，程序启动的入口点，提供插件加载、命令路由、事件监听等功能。
- [**saltify-web**](./saltify-web) - Saltify 的 Web UI，提供插件管理、Bot 状态监控等功能。
- [**saltify-api**](./saltify-api) - 框架 API 模块，包含 Bot 开发所需的基础数据结构（如好友、群、群成员、消息、消息段）、事件的定义以及插件 DSL 的核心 API，**需要在开发插件时引入**。
- **adapter** - 目前包含以下实现：
  - [**saltify-adapter-lagrange**](./saltify-adapter-lagrange) - 用 Kotlin 编写的 NTQQ 引擎
  - [**saltify-adapter-milky**](./saltify-adapter-milky) - 对接其他支持 [Milky](https://milky.ntqqrev.org/) 的协议端
- [**saltify-utils**](./saltify-utils) - 私有模块，提供框架和适配器通用的工具类和函数。

## 插件 DSL 示例

```kotlin
class Config(
    val echoPrefix: String = "::"
)

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