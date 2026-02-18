plugins {
    kotlin("jvm") version "2.3.10" apply false
    kotlin("multiplatform") version "2.3.10" apply false
}

group = "org.ntqqrev"
version = "1.1.0"

subprojects {
    repositories {
        mavenCentral()
    }
}
