<div align="center">

<h1>milky-kt-sdk</h1>

[Milky](https://milky.ntqqrev.org/) 协议的 Kotlin SDK

</div>

## 特性

- 基于 Ktor Client 实现
- 兼容 JVM / Native (Windows, macOS, Linux) 平台
- 支持 Milky 协议的所有功能
  - 例外：不支持通过 WebHook 事件推送监听事件

## 用例

见 [ClientTest.kt](/src/commonTest/kotlin/org/ntqqrev/milky/ClientTest.kt)

### 初始化

```kotlin
val client = MilkyClient(
    addressBase = "http://localhost:3000",
    eventConnectionType = EventConnectionType.WebSocket,
    // accessToken = "..."
)
```

### 调用 API

```kotlin
// API 无参数
val loginInfo = client.callApi(ApiEndpoint.GetLoginInfo)

// API 有参数
val userProfile = client.callApi(
    ApiEndpoint.GetUserProfile,
    GetUserProfileInput(userId = loginInfo.uin)
)
```

### 监听事件

```kotlin
// 连接事件服务
client.connectEvent()

// 监听消息事件，并创建 Job 以便后续取消监听
val job = launch {
    client.subscribe {
        if (it is Event.MessageReceive) {
            when (it.data) {
                is IncomingMessage.Group -> {
                    println("Group message from ${it.data.peerId} by ${it.data.senderId}:")
                    println(milkyJsonModule.encodeToString(it.data.segments))
                }

                else -> {}
            }
        }
    }
}

// 退出时取消监听
job.cancel()
client.disconnectEvent()
```