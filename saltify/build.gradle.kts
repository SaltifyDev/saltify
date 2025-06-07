plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(project(":saltify-api"))
    implementation(project(":saltify-adapter-lagrange"))
}

application {
    mainClass = "org.ntqqrev.saltify.MainKt"
}