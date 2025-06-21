plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.jacksonModuleKotlin)
    implementation(kotlin("reflect"))
    implementation(project(":saltify-api"))
    implementation(project(":saltify-adapter-lagrange"))
}

application {
    mainClass = "org.ntqqrev.saltify.MainKt"
}