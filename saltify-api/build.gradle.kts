plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    api(libs.kotlinxDatetime)
    api(libs.kotlinxCoroutines)
    api(libs.ktorm)
}