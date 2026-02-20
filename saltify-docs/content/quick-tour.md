# 快速上手

欢迎来到 Saltify 的文档！本页面将会通过简单的示例介绍如何快速上手 Saltify。

## 开始之前

### 添加 `saltify-core` 依赖

[![Maven Central](https://img.shields.io/maven-central/v/org.ntqqrev/saltify-core?label=Maven%20Central&logo=maven)](https://central.sonatype.com/artifact/org.ntqqrev/saltify-core)

在使用 Saltify 之前，确保你已经将 `saltify-core` 作为依赖添加到你的项目中。

对于使用 Gradle 的 Kotlin JVM 项目，可以在 `build.gradle.kts` 中添加以下内容：

```kotlin
dependencies {
    implementation("org.ntqqrev:saltify-core:$saltifyVersion")
}
```

对于 Kotlin Multiplatform 项目，可以将 `saltify-core` 添加到 `commonMain` 的依赖中：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.ntqqrev:saltify-core:$saltifyVersion")
        }
    }
}
```

最新版本号如上面的 Badge 所示。

### 添加 Ktor Client 引擎依赖

Saltify 使用 Ktor 进行 HTTP 请求和事件连接，因此你需要根据你的项目平台添加相应的 Ktor Client 引擎依赖。不同平台可用的 Ktor Client 引擎见 [Ktor 的有关页面](https://ktor.io/docs/client-engines.html)。

例如，对于 JVM 项目，可以将 `ktor-client-cio` 添加到依赖中：

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
}
```

### 配置协议端

使用 Saltify 进行开发需要你对 [Milky 协议](https://milky.ntqqrev.org/)有基本的了解，并且需要有一个运行的 Milky 协议服务器（即 QQ 协议端）。参考 [Awesome Milky](https://milky.ntqqrev.org/awesome#%E5%8D%8F%E8%AE%AE%E5%AE%9E%E7%8E%B0) 页面的相关部分来了解支持 Milky 的协议端，并按照协议端的文档进行配置。

## 初始化

使用 `SaltifyApplication` 创建一个 Saltify 客户端实例：

```kotlin
val client = SaltifyApplication {
    addressBase = "http://localhost:3000" // 在协议端中配置的地址
    accessToken = "..." // 在协议端中配置的 Access Token，如果协议端启用了 Access Token 验证，则需要提供。
    
    // 事件服务相关配置
    eventConnection {
        type = EventConnectionType.WebSocket // 连接事件服务的方式，支持 WebSocket 和 Server-Sent Events
        autoReconnect = true // 是否自动重连
    }
}

// ...

// 释放 client
client.close()
```

## 调用 API

`SaltifyApplication` 暴露了许多 API 来与协议端进行交互，例如获取登录信息、发送消息等：

```kotlin
val loginInfo = client.getLoginInfo()

client.sendGroupMessage(123456789L) {
    text("Hello from Saltify!")
    image("https://example.com/example.jpg")
    image("https://example.com/example2.jpg", subType = "sticker")
}
```

## 监听事件

要启动事件监听，首先需要显式地连接事件服务（事件服务不会在 `SaltifyApplication` 初始化时自动连接），然后使用 `on` 函数监听事件：

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
client.disconnectEvent()
```

`client.disconnectEvent()` 不会被 `client.close()` 自动调用，事件服务可复用。

## 定义插件

可以通过 `createSaltifyPlugin` 定义一个插件。在其中同样可以使用 `on` 监听事件，也可以使用 `onStart` 和 `onStop` 定义插件的启动和停止逻辑，还可以使用 `command` 定义命令：

```kotlin
val myPlugin = createSaltifyPlugin("test") {
    onStart {
        // ...
    }

    on<Event.GroupMemberIncrease> {
        // ...
    }

    // /order <id> <note>
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

        // 使用 Typed error 处理命令参数类型不匹配，命令参数缺失等情况
        onFailure {
            respond {
                text("Command run failed: $it")
            }
        }
    }
}
```

可以在 SaltifyApplication 的初始化块内声明使用之前定义的插件，也可以直接定义一个匿名插件：

```kotlin
val client = SaltifyApplication {
    install(myPlugin)
    
    plugin {
        // ...
    }
}
```

## 异常处理

### 全局异常处理

Saltify 会将插件和事件监听器中的异常通过 `exceptionFlow` 抛出。默认情况下，所有异常都会被无视。
你可以收集 (通常来说是必须) `exceptionFlow` 来实现自定义逻辑。

```kotlin
launch {
    client.exceptionFlow.collect { (context, exception) ->
        val component = context.saltifyComponent!!

        when (component.type) {
            SaltifyComponentType.Application -> throw exception
            else -> logger.error(
                "Saltify component ${component.name}(${component.type}) occurred an exception: ",
                exception
            )
        }
    }
}
```

### 事件服务连接状态流转

可以通过 `eventConnectionStateFlow` 收集事件服务连接的状态。由连接断开触发的异常不会进入 `exceptionFlow`。

```kotlin
launch {
    client.eventConnectionStateFlow.collect {
        when (it) {
            is EventConnectionState.Disconnected if it.throwable != null -> throw it.throwable
            else -> println("Event connection state changed to: $it")
        }
    }
}
```
