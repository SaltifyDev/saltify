# 指令开发

Saltify 提供了一套类型安全的指令构建 DSL，支持参数解析、子指令以及特定上下文的执行逻辑。

## 定义指令

以下是一个简单的指令定义：

```kotlin
client.command("order", prefix = "/") {
    description = "创建一个订单"

    // 定义一个 Int 类型的参数
    val id = parameter<Int>("id")
    // 定义一个贪婪字符串参数，即将之后所有内容视为一个参数
    val note = greedyStringParameter("note")

    onExecute {
        // some logic...
        respond("订单 #${id.value} 已创建\nNote：${note.value}")
    }

    onFailure { error ->
        respond { /** ... */ }
    }
}
```

onFailure 块是**解析**失败的处理，注意这里不是异常，而是指令参数类型不匹配、过多参数、参数缺失等预期内错误。

这里的 error 是一个 `Typed error`，简单来说就是密封类。你可以用 when 判断具体是什么问题，并加以处理。

默认情况下，如果不定义这个块，会忽视所有解析错误，并对指令不做回复。

> [!TIP]
> 
> 与 `on` 一样，command 在插件初始化块中也是可以，并且推荐使用的。不需要传 `client`。

## 上下文隔离

指令可以针对不同的聊天场景执行不同的逻辑：

```kotlin
client.command("info") {
    onGroupExecute {
        respond("这是群聊专用的信息！")
    }
    
    onPrivateExecute {
        respond("这是私聊专用的信息！")
    }
    
    onExecute {
        // 只有当没有定义上述两个块时，或者在其他未知上下文中，才会执行兜底逻辑
    }
}
```

> [!NOTE]
> 
> `onGroupExecute` 和 `onPrivateExecute` 的优先级**高于** `onExecute`。如果群聊触发了指令且你定义了 `onGroupExecute`，那么 `onExecute` 中的兜底逻辑将**不会**被执行。

## 子指令

通过 `subCommand` 函数，可以方便地定义一个任意嵌套层级的指令树：

```kotlin
command("math") {
    // /math add <num1> <num2>
    subCommand("add") {
        val a = parameter<Int>("a")
        val b = parameter<Int>("b")

        onExecute {
            val result = a.value + b.value
            respond(result)
        }
    }

    // /math power <base>
    subCommand("power") {
        val base = parameter<Int>("base")
        onExecute {
            val value = base.value
            respond("$value 的平方是 ${value * value}")
        }
    }
}
```

## 上下文交互

在指令执行上下文中，你可以挂起当前协程，等待该用户的下一条回复。

```kotlin
client.command("shutdown") {
    onExecute {
        respond("真的要关机吗？")
        
        // 等待该用户在同一上下文中发送的下一条消息
        val replyEvent = awaitNextMessage(timeout = 30.seconds)
        
        if (replyEvent == null) {
            respond("等待超时")
        } else {
            respond("你回复了我，但这不重要，无论如何，我都不想关掉我自己！")
        }
    }
}
```

## Requirements

Requirements 是用于指令鉴权的语法，合理使用可以减少很多 onExecute 块内的判断逻辑。

```kotlin
client.command("stop") {
    require { user(3650502250, 3521766148) + group(570335215) }

    onExecute {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            client.disconnectEvent()
            client.close()
        }
    }
}
```

效果不言自明，自定义这样的 requirement 函数也很简单：

```kotlin
fun SaltifyCommandRequirementContext.user(vararg targetId: Long) =
    CommandRequirement {
        context.event.senderId in targetId
    }
```

> [!TIP]
> 
> 在 require 块内，还可以通过 `or` `and` `not` 拼接 requirements！
