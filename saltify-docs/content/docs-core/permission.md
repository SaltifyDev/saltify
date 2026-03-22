# 权限管理

Saltify 提供了内置的一套简易权限管理方案。默认支持如下几个权限等级，括号内是权重(大的更重)：

- Restricted – 受限制 (Int.MIN_VALUE)
- Everyone – 所有人 (0)
- Admin – 群管理员 (100)
- Owner – 群主 (200)
- SuperUser – 最高权限 (Int.MAX_VALUE)

可以这样使用权限 API:

```kotlin
client.command("stop") {
    require { perm(PermissionLevel.SuperUser) }
    
    onExecute { /** ... */ }
}

client.on<Event.MessageReceive> { event ->
    if (event.senderPermissionLevel >= PermissionLevel.Admin) {
        TODO()
    }
}
```

需要注意的是，requirements 判定失败是默认返回的，所以上例1所示适用范围可能较窄。
