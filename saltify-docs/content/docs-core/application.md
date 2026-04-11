# 应用配置

## 初始化 `SaltifyApplication`

可以通过以下方式创建 Saltify 实例。这里仅展示了核心配置。

```kotlin
val client = SaltifyApplication {
    connection {
        baseUrl = "http://localhost:3000"
        accessToken = "your_token" // 访问令牌

        // 事件服务连接配置
        events {
            type = EventConnectionType.WebSocket // 可选 WebSocket 或 SSE
            autoReconnect = true
        }
    }

    bot {
        superUsers = mutableSetOf(3650502250, 3521766148)
    }
}.start()
```

事件服务连接不会自动建立，你需要手动调用 `connectEvent()` 才会真正开始监听事件。

```kotlin
val client = SaltifyApplication { /* ... */ }.start()

client.connectEvent()

// 其他逻辑

// 断开事件连接服务，可复用
client.disconnectEvent()
```

另外，应用和插件都有生命周期管理。在程序退出时，务必调用 `close()` 方法释放资源，这会触发所有已加载插件的 `onStop` 钩子并关闭 HTTP 客户端。

## 全局异常处理

Saltify 提供了用于监控未显式捕获的异常的 Flow，可以通过以下代码实现自定义逻辑：

```kotlin
client.exceptionFlow.collect { (context, exception) ->
    val component = context.saltifyComponent!!

    when (component.type) {
        SaltifyComponentType.Application -> throw exception
        else -> println(
            "组件 ${component.name}(${component.type}) 抛出了一个异常: " +
                    exception.stackTraceToString()
        )
    }
}
```

如上所见，所有 Saltify 创建的协程都有一个名为 `SaltifyComponent` 的上下文，它包含了组件的元信息。例如这里，Application 代表应用实例。

## 事件服务状态监听

Saltify 还提供了一个 Flow 用于监控事件服务连接状态的变更：

```kotlin
client.eventConnectionStateFlow.collect { state ->
    println("连接状态变更: $state")
}
```

需要注意的是，由连接状态断开引起的异常不会出现在 Application 异常流，而是出现在此流中。
