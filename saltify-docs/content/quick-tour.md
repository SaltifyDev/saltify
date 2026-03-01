# 快速上手

欢迎来到 Saltify 的文档！本页面将会通过简单的示例介绍如何快速上手 Saltify。

## 开始之前

### 添加 `saltify-core` 依赖

[![Maven Central](https://img.shields.io/maven-central/v/org.ntqqrev/saltify-core?label=Maven%20Central&logo=maven)](https://central.sonatype.com/artifact/org.ntqqrev/saltify-core)

对于使用 Gradle 的 Kotlin 项目，可以在 `build.gradle.kts` 中添加以下内容：

```kotlin
dependencies {
    implementation("org.ntqqrev:saltify-core:$saltifyVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
}
```

你可以选择为 Ktor client 使用不同的引擎而不是 cio，这里只是一个推荐。

> [!warning]
> 本项目仍处于开发阶段，**强烈建议自行构建项目并依赖你的本地构建**，目前本文档将基于最新提交而不是最新发行版。待项目稳定会区分稳定文档与开发文档。

## 初始化 `SaltifyApplication`

```kotlin
val client = SaltifyApplication {
    connection {
        baseUrl = "http://localhost:3000"
        accessToken = "your_token"

        // 事件服务相关配置
        events {
            type = EventConnectionType.WebSocket
            autoReconnect = true
        }
    }
}
```

更多内容请参见[核心配置](guide/application.md)。请在浏览完本页后**一定仔细阅读**这一页。

## 调用 API

```kotlin
// 获取登录信息
val loginInfo = client.getLoginInfo()

// 发送群消息
client.sendGroupMessage(123456789L) {
    text("Hello from Saltify!")
    image("https://example.com/example.jpg")
}
```

更多 API 请参见项目源码与 [Milky 文档](https://milky.ntqqrev.org/)。

## 定义插件

插件是一系列功能的集合，建议将功能逻辑封装在插件中。

```kotlin
val myPlugin = SaltifyPlugin("my-plugin") {
    onStart {
        println("插件已启动")
    }

    on<Event.GroupMemberIncrease> { event ->
        println("新成员加入: ${event.data.userId}")
    }
}

val client = SaltifyApplication {
    // ...

    // 在初始化时安装插件
    install(myPlugin)
    
    // 也可以直接定义插件并安装
    plugin {
        // ...
    }
}
```

更多内容请参见[插件开发](guide/plugin.md)。

## 定义指令

```kotlin
client.command("say") {
    val content = greedyStringParameter("content", "要重复的内容")

    onExecute {
        respond {
            text(content.value)
        }
    }
}
```

更多内容请参见[指令系统](guide/command.md)。
