![banner](https://socialify.git.ci/SaltifyDev/saltify/image?description=1&font=Bitter&forks=1&issues=1&logo=https%3A%2F%2Favatars.githubusercontent.com%2Fu%2F208890061%3Fs%3D400%26u%3D49580f4a3a7837cdd8d43a532d0789b2488a2ffb%26v%3D4&name=1&owner=1&pattern=Plus&pulls=1&stargazers=1&theme=Light)

<div align="center">

[![QQ 群](https://img.shields.io/badge/QQ_Group-570335215-green?logo=qq)](https://qm.qq.com/q/C04kPQzayk)
[![Telegram](https://img.shields.io/badge/Telegram-WeavingStar-orange?logo=telegram)](https://t.me/WeavingStar)
[![Maven Central](https://img.shields.io/maven-central/v/org.ntqqrev/saltify?label=Maven%20Central&logo=maven)](https://central.sonatype.com/artifact/org.ntqqrev/saltify-core)

</div>

## 特性

- 基于 Ktor Client 和 Kotlinx Serialization 实现
- 兼容 JVM / Native / JS / WASM 平台
- 支持 Milky 协议的所有功能
  - 例外：不支持通过 WebHook 事件推送监听事件
- 支持插件化开发、命令路由、事件监听等功能的 DSL

> [!tip]
> 使用时，你需要在项目中添加一个 Ktor Client 引擎依赖，例如 `ktor-client-cio`、`ktor-client-okhttp` 等。

## 快速开始

### 初始化

```kotlin
val client = SaltifyApplication {
    addressBase = "http://localhost:3000"
    eventConnectionType = EventConnectionType.WebSocket
    // accessToken = "..."
}

// ...

// 释放 client
client.close()
```

### 调用 API

```kotlin
val loginInfo = client.getLoginInfo()

client.sendGroupMessage(123456789L) {
    text("Hello from Saltify!")
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
// 断开事件服务
// client.disconnectEvent() 不会被 client.close() 自动调用，事件服务可复用。
client.disconnectEvent()
```

### 定义插件

```kotlin
val myPlugin = createSaltifyPlugin("test") {
    onStart {
        // ...
    }

    on<Event.GroupMemberIncrease> {
        // ...
    }

    //order <id> <note>
    command("order") {
        // id.value 的类型为 Int
        val id = parameter<Int>("id")
        // 贪婪匹配，即后面的参数视为一个参数
        val note = greedyStringParameter("note")

        onExecute {
            respond {
                text("Order #${id.value} created\nnote：${note.value}")
            }
        }

        // 优先级高于 onExecute，同样还有 onPrivateExecute
        onGroupExecute {
            // ...
        }

        // 使用 Typed error 处理命令参数类型不匹配，命令参数缺失等情况。
        onFailure {
            respond {
                text("Command run failed: $it")
            }
        }
    }
}
```

可以在 SaltifyApplication 内声明使用这个插件，或者直接在其中定义插件：

```kotlin
val client = SaltifyApplication {
    // ...
    install(myPlugin)
    
    plugin {
        // ...
    }
}
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

完整的用例见 [PluginTest.kt](/saltify-core/src/jvmTest/kotlin/org/ntqqrev/saltify/PluginTest.kt)。
