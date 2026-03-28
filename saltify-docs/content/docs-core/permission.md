# 权限管理

Saltify 提供了内置的一套简易权限管理方案。默认支持如下几个权限等级，括号内是权重(大的更重)：

- **Restricted** – 受限制 (Int.MIN_VALUE)
- **Everyone** – 所有人 (0)
- **SuperUser** – 最高权限 (Int.MAX_VALUE)

可以这样使用权限 API:

```kotlin
SaltifyBotConfig.superUsers += 123456789
SaltifyBotConfig.restrictedUsers += 987654321

client.command("stop") {
    require { perm(PermissionLevel.SuperUser) }
    
    onExecute { /** ... */ }
}

client.on<Event.MessageReceive> { event ->
    if (permissionLevelOf(event.senderId) >= PermissionLevel.SuperUser) {
        TODO()
    }
}
```

需要注意的是，requirements 判定失败时会静默返回，因此上述第一种用法的适用场景可能相对有限。
