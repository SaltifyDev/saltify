plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.logging)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.kotlinReflect)
    implementation(libs.saltifyProtobuf)
    implementation(project(":saltify-api"))
}