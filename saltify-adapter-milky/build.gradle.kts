plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.jacksonModuleKotlin)
    implementation(kotlin("reflect"))
    implementation(project(":saltify-api"))
    implementation(project(":saltify-utils"))
}