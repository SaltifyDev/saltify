@file:OptIn(ExperimentalWasmDsl::class)

package buildsrc.convention

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

kotlin {
    androidNativeArm64()
    androidNativeX64()
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
    wasmJs {
        browser()
        nodejs()
        d8()
    }
    wasmWasi {
        nodejs()
    }

    jvmToolchain(21)
}
