plugins {
    kotlin("jvm") version "2.3.10"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.5")
}
