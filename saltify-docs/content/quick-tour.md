# 快速上手

欢迎来到 Saltify 的文档！

## 开始之前

### 添加 `saltify-core` 依赖

[![Maven Central](https://img.shields.io/maven-central/v/org.ntqqrev/saltify-core?label=Maven%20Central&logo=maven)](https://central.sonatype.com/artifact/org.ntqqrev/saltify-core)

对于使用 Gradle 的 Kotlin 项目，可以在 `build.gradle.kts` 中添加以下内容：

```kotlin
dependencies {
    implementation("org.ntqqrev:saltify-core:$saltifyVersion")

    // 你可以选择为 Ktor client 使用不同的引擎而不是 cio，这里只是一个推荐。
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    // 日志相关实现。同样，可以选择你所需要的平台日志实现。
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}
```

> [!warning]
> 本项目仍处于开发阶段，**强烈建议自行构建项目并依赖你的本地构建**，目前本文档将基于最新提交而不是最新发行版。待项目稳定会区分稳定文档与开发文档。

在完成了以上配置后，点击页面下方的按钮，了解如何使用 Saltify 构建应用！
