# 核心配置与异常处理

## 应用实例化与基础配置

可以通过以下方式快速配置一个 Saltify 实例。这里仅展示了核心配置。

```kotlin
val client = SaltifyApplication {
    connection {
        baseUrl = "http://localhost:3000"
        accessToken = "your_token" // 选填：访问令牌（不用加 Bearer）

        // 事件服务连接配置
        events {
            type = EventConnectionType.WebSocket // 可选 WebSocket 或 SSE
            autoReconnect = true
        }
    }
}
```

事件服务连接不会自动建立，你需要手动调用 `connectEvent()` 才会真正开始监听事件。

```kotlin
suspend fun main() {
    val client = SaltifyApplication { /* ... */ }

    client.connectEvent() 
    
    // 其他逻辑

    // 断开事件连接服务，可复用
    client.disconnectEvent()
}
```

另外，应用和插件都有生命周期管理。在程序退出时，务必调用 `close()` 方法释放资源，这会触发所有已加载插件的 `onStop` 钩子并关闭 HTTP 客户端。

## 全局异常处理与事件服务状态

Saltify 提供了用于监控连接状态和异常的 Flow，一般必须收集以实现自定义逻辑，否则**全部异常都会被无视**。

```kotlin
launch {
    client.eventConnectionStateFlow.collect { state ->
        println("连接状态变更: $state")
    }
}

launch {
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
}
```
