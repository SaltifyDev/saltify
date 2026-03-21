# 快速开始

欢迎来到 Saltify 的文档！

Saltify 是一个跨平台、可扩展的 Kotlin QQ Bot 框架 & Milky SDK。

- **现代化基础设施** - 基于 Kotlinx 全家桶实现，高效稳定
- **跨平台支持** - 兼容 Kotlin JVM / Native / JS / Wasm 平台，一套代码覆盖更多运行环境
- **丰富功能** - 支持 Milky 协议所有 API / 事件，支持插件化开发、指令路由、事件监听等 DSL

## 开始之前

### 添加 Saltify 依赖

对于使用 Gradle 的 Kotlin 项目，可以在 `build.gradle.kts` 中添加以下内容：

```kotlin
dependencies {
    // 核心模块
    implementation("org.ntqqrev:saltify-core:1.2.0-RC2.1")

    // 你可以选择为 Ktor client 使用不同的引擎而不是 cio，这里只是一个推荐。
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    // 日志相关实现。同样，可以选择你所需要的平台日志实现。
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}
```

> [!IMPORTANT]
> 
> 本项目仍处于开发阶段，**强烈建议自行构建项目并依赖你的本地构建**，目前本文档将基于最新提交而不是最新发行版。待项目稳定会区分稳定文档与开发文档。

在完成了以上配置后，点击页面下方的按钮，了解如何使用 Saltify 构建应用！
