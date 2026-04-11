# 插件开发

使用插件是 Saltify 组织代码逻辑的最佳实践。通过将相关联的事件监听和指令封装在一个插件中，可以大幅提升代码的可维护性。

## 定义插件

可以预先定义一个 `SaltifyPlugin`：

```kotlin
class MyPluginConfig(
    var reply: String = "Hello!",
    var enableFeatureX: Boolean = true
)

val myPlugin = SaltifyPlugin("my-awesome-plugin", ::MyPluginConfig) { config ->
    // 插件加载完成后的初始化逻辑
    onStart {
        println("插件已启动！")
    }

    // 应用关闭时的清理逻辑
    onStop {
        println("插件已停止！")
    }

    // 监听特定事件
    on<Event.GroupMemberIncrease> {
        respond { text(config.reply) }
    }

    // 监听以正则表达式匹配的消息
    regex("""BV1\w{9}""") { matches ->
        respond {
            text(matches.joinToString { it.value })
        }
    }
}
```

> [!TIP]
> 
> 所有 `respond` 函数都有一个为纯文本情况提供的简写形式。如上例，完全可以写成 `event.respond(config.reply)`。


## 安装插件

```kotlin
val client = SaltifyApplication {
    // 1. 安装外部插件
    install(myPlugin) {
        reply = "Hello!!"
    }
    
    // 2. 或者直接内联定义
    plugin("another-plugin") {
        onStart { /* ... */ }
    }
}
```

外部定义的插件的配置是可选的，在应用初始化块内定义的插件不可配置。
