package org.ntqqrev.saltify

import kotlinx.coroutines.delay
import kotlin.io.path.Path

suspend fun main() {
    println(SaltifyApp.banner)
    println("Welcome to ${SaltifyApp.name} v${SaltifyApp.version}")

    SaltifyAppContainer.startSaltify(Path("."))
    delay(Long.MAX_VALUE)
}