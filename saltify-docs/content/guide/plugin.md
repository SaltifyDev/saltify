# 插件开发

使用插件是 Saltify 组织代码逻辑的最佳实践。通过将相关联的事件监听和指令封装在一个插件中，可以大幅提升代码的可维护性。

## 定义插件

你可以预先定义一个 `SaltifyPlugin`，或者在应用配置时直接使用 `plugin {}` 块。

```kotlin
class MyPluginConfig(
    var reply: String = "Hello!",
    var enableFeatureX: Boolean = true
)

val myPlugin = SaltifyPlugin("my-awesome-plugin", ::MyPluginConfig) { config ->
    // 插件加载完成后的初始化逻辑
    // 这里的 `name` 就是 "my-awesome-plugin"
    onStart {
        println("[$name] 插件已启动！")
    }

    // 应用关闭时的清理逻辑
    onStop {
        println("[$name] 插件已停止！")
    }

    // 监听特定事件
    on<Event.GroupMemberIncrease> { event ->
        event.respond { text(config.reply) }
    }
}
```

## 安装插件

```kotlin
val client = SaltifyApplication {
    // 安装插件
    install(myPlugin) {
        reply = "Hello!!"
    }
    
    // 或者直接内联定义
    plugin("another-plugin") {
        onStart { /* ... */ }
    }
}
```

外部定义的插件的配置是可选的，在应用初始化块内定义的插件不可配置。
