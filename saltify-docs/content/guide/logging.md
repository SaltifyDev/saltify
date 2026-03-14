# 日志配置

Saltify 使用 KtorSimpleLogger 输出运行时日志。

## JVM 平台

在 JVM 平台上，使用 SLF4J 作为日志框架，这里以 Logback 为例：

```kotlin
dependencies {
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}
```

### 配置日志

在 `src/main/resources` 目录下创建 `logback.xml` 配置文件，以下是示例配置：

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss} %highlight([%-5level]) %cyan(%logger{36}) - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

关于其他自定义配置，可以参照相关日志框架的文档。

## 其他平台

对于其他非 JVM 平台，Saltify 会调用 println 等相关基础 API 直接输出到控制台。

## 不使用日志

如果不需要日志功能，可以选择不添加任何日志实现依赖。在这种情况下：

- **JVM 平台**：日志调用会被忽略，不会有任何输出
- **其他平台**：仍会输出到控制台

## 自行调用 Logger

在插件上下文、指令上下文中可以直接使用 `logger`:

```kotlin
val myPlugin = SaltifyPlugin("my-plugin", ::Config) { config ->
    onStart {
        logger.info("插件已启动")
    }

    on<Event.MessageReceive> {
        logger.debug("收到消息: ${it.segments.plainText}")
    }

    command("test") {
        onExecute {
            logger.info("123")
            // ...
        }
    }
}
```

此时 logger 的 name 分别为: `Saltify/plugin:$pluginName` 与 `Saltify/cmd:$commandName`。

> [!tip]
> 对于匿名插件，Saltify 会生成 `Saltify/plugin:anonymous-XXXX` 作为 logger name。
