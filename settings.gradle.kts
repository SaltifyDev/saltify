dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":saltify")
include(":saltify-api")
include(":saltify-adapter-lagrange")
include(":saltify-adapter-milky")
include(":saltify-utils")

rootProject.name = "saltify"