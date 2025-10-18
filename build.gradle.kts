plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("maven-publish")
    id("signing")
}

group = "org.ntqqrev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    macosX64()
    macosArm64()
    linuxArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation(ktorLibs.client.core)
            implementation(ktorLibs.client.contentNegotiation)
            implementation(ktorLibs.client.serialization)
            implementation(ktorLibs.client.websockets)
            implementation(ktorLibs.serialization.kotlinx.json)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        jvmMain.dependencies {
            implementation(ktorLibs.client.cio)
        }
        appleMain.dependencies {
            implementation(ktorLibs.client.darwin)
        }
        linuxMain.dependencies {
            implementation(ktorLibs.client.curl)
        }
        mingwMain.dependencies {
            implementation(ktorLibs.client.winhttp)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    jvmToolchain(21)
}