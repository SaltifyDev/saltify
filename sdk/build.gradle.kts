@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    kotlin("multiplatform") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("com.google.devtools.ksp") version "2.3.5"
}

dependencies {
    add("kspCommonMainMetadata", project(":ksp"))
}

kotlin {
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    js(IR) {
        browser()
        nodejs()
    }
    jvm()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    wasmJs {
        browser()
        nodejs()
        d8()
    }
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(ktorLibs.client.core)
                implementation(ktorLibs.client.contentNegotiation)
                implementation(ktorLibs.client.serialization)
                implementation(ktorLibs.client.websockets)
                implementation(ktorLibs.serialization.kotlinx.json)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
                api("org.ntqqrev:milky-kt-types:1.1.0")
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(ktorLibs.client.cio)
        }
    }

    jvmToolchain(21)

    explicitApi()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = rootProject.group.toString(),
        artifactId = "milky-kt-sdk",
        version = rootProject.version.toString()
    )

    pom {
        name = rootProject.name
        description = "Milky SDK for Kotlin Multiplatform"
        url = "https://github.com/SaltifyDev/milky-kt-sdk"
        inceptionYear = "2025"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "Wesley-Young"
                name = "Wesley F. Young"
                email = "wesley.f.young@outlook.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/SaltifyDev/milky-kt-sdk.git"
            developerConnection = "scm:git:ssh://github.com/SaltifyDev/milky-kt-sdk.git"
            url = "https://github.com/SaltifyDev/milky-kt-sdk"
        }
    }
}

tasks.withType<KotlinCompile> {
    dependsOn(":sdk:kspCommonMainKotlinMetadata")
}

tasks.withType<KotlinNativeCompile> {
    dependsOn(":sdk:kspCommonMainKotlinMetadata")
}

tasks.withType<KotlinJsCompile> {
    dependsOn(":sdk:kspCommonMainKotlinMetadata")
}

tasks.matching { it.name.lowercase().endsWith("sourcesjar") }.configureEach {
    dependsOn(":sdk:kspCommonMainKotlinMetadata")
}