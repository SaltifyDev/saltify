# Milky SDK for Kotlin

[![Maven Central](https://img.shields.io/maven-central/v/org.ntqqrev/milky-kt-sdk?label=Maven%20Central&logo=maven)](https://central.sonatype.com/artifact/org.ntqqrev/milky-kt-sdk)
![kotlinx-serialization](https://img.shields.io/badge/kotlinx--serialization-1.10.0-blue?logo=kotlin&logoColor=white)
![ktor](https://img.shields.io/badge/ktor-3.4.0-blue?logo=ktor&logoColor=white)

## 特性

- 基于 Ktor Client 和 Kotlinx Serialization 实现
- 兼容 JVM / Native / JS / WASM 平台
- 支持 Milky 协议的所有功能
  - 例外：不支持通过 WebHook 事件推送监听事件

> [!tip]
> 使用时，你需要在项目中添加一个 Ktor Client 引擎依赖，例如 `ktor-client-cio`、`ktor-client-okhttp` 等。

## 用例

见 [ClientTest.kt](/sdk/src/jvmTest/kotlin/org/ntqqrev/milky/ClientTest.kt)

### 初始化

```kotlin
val client = MilkyClient {
    addressBase = "http://localhost:3000"
    eventConnectionType = EventConnectionType.WebSocket
    // accessToken = "..."

    // 直接定义插件
    plugin("name") {
        // ...
    }

    // 导入插件
    install(myPlugin)
}

// 释放 client
client.close()
```

### 定义插件

```kotlin
val myPlugin = milkyPlugin {
    onStart {
        // ...
    }

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

    // ...
}
```

### 调用 API

```kotlin
val loginInfo = client.getLoginInfo()

client.sendGroupMessage(123456789L) {
    text("Hello from Milky🥛!")
    image("https://example.com/example.jpg")
    image("https://example.com/example2.jpg", subType = "sticker")
}
```

### 监听事件

```kotlin
// 连接事件服务
client.connectEvent()

// 监听消息事件，并创建 Job 以便后续取消监听
val job = launch {
    client.on<Event.MessageReceive> {
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
}

// 退出时取消监听
job.cancel()
// tips: disconnectEvent() 不会被 client.close() 自动调用
client.disconnectEvent()
```

### 异常处理

```kotlin
runBlocking {
    client.on<IllegalStateException> { _, e ->
        println("Receive exception: ${e.message}")
        if (e.message != "test exception") {
            this@runBlocking.cancel(CancellationException(e.message, e))
        }
    }
}
```

关于 `MilkyCommandDsl.onFailure` 与 `MilkyClient.on` 的 Throwable 变体: 前者用于预料内的异常, 即命令参数缺失等。后者用于全局异常捕捉。

- 没有在 command 作用域内定义 onFailure 时，*预料内的异常*会被忽视，其他异常重新抛出。
- 在 command 作用域内定义了 onFailure 时，只会传*预料内的异常*，其他异常重新抛出。 

就是说可以把*预料内的异常*当成一种 CancellationException。
