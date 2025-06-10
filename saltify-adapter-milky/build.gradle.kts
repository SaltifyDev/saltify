plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(project(":saltify-api"))
}