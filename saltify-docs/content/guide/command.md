# 指令系统

Saltify 提供了一套类型安全的指令构建 DSL，支持参数解析、子指令以及特定上下文的执行逻辑。

## 基础指令与参数解析

你可以为指令定义类型安全的参数，并通过 `.value` 快速获取。

```kotlin
client.command("order", prefix = "/") {
    // 定义一个 Int 类型的参数
    val id = parameter<Int>("id")
    // 定义一个贪婪字符串参数，即将之后所有内容视为一个参数
    val note = greedyStringParameter("note")

    onExecute {
        respond {
            text("Order #${id.value} created\nnote：${note.value}")
        }
    }

    onFailure { error ->
        respond { /** ... */ }
    }
}
```

onFailure 块是**解析**失败的处理，注意这里不是异常，而是指令参数类型不匹配、过多参数、参数缺失等预期内错误。

这里的 error 是一个 `Typed error`，说的简单点，密封类。你可以用 when 判断到底是什么问题，并加以处理。

默认情况下，如果不定义这个块，会忽视所有解析错误，并对指令不做回复。

> [!tip]
> 与 `on` 一样，command 在插件初始化块中也是可以使用的，并且不用手动传 client。

## 上下文隔离

指令可以针对不同的聊天场景执行不同的逻辑：

```kotlin
client.command("info") {
    onGroupExecute {
        respond { text("这是群聊专用的信息！") }
    }
    
    onPrivateExecute {
        respond { text("这是私聊专用的信息！") }
    }
    
    onExecute {
        // 只有当没有定义上述两个块时，或者在其他未知上下文中，才会执行兜底逻辑
    }
}
```

> [!tip]
> `onGroupExecute` 和 `onPrivateExecute` 的优先级**高于** `onExecute`。如果群聊触发了指令且你定义了 `onGroupExecute`，那么 `onExecute` 中的兜底逻辑将**不会**被执行。

## 上下文交互

在指令执行上下文中，你可以挂起当前协程，等待该用户的下一条回复。

```kotlin
client.command("shutdown") {
    onExecute {
        respond { text("真的要关机吗？") }
        
        // 等待该用户在同一上下文中发送的下一条消息（默认 30 秒超时）
        val replyEvent = awaitNextMessage(timeout = 30.seconds)
        
        if (replyEvent == null) {
            respond { text("等待超时") }
        } else {
            respond { text("你回复了我，但这不重要，无论如何，我都不想关掉我自己！") }
        }
    }
}
```
