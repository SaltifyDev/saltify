plugins {
    kotlin("jvm") version "2.3.0"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.4")
}
