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

val myPlugin = milkyPlugin {
    onStart {
        // ...
    }
  
    command("ping") {
        it.reply {
            text("Pong!")
        }
    }

    // ...
}
```

### 调用 API

```kotlin
// API 无参数
val loginInfo = client.getLoginInfo()

// API 有参数
val userProfile = client.getUserProfile(/* userId = */ loginInfo.uin)
```

### 发送消息

```kotlin
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
client.disconnectEvent()

// 彻底关闭 CoroutineScope
client.close()
```
