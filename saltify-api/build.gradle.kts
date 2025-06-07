plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    compileOnly(libs.kotlinxDatetime)
    compileOnly(libs.kotlinxCoroutines)
}