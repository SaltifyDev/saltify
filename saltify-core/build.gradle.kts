import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.ksp)
}

dependencies {
    add("kspCommonMainMetadata", project(":saltify-processor"))
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines.core)
                api(libs.milky.types)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmTest.dependencies {
            implementation(libs.ktor.client.cio)
        }
    }

    explicitApi()
}

tasks.withType<KotlinCompile> {
    dependsOn(":saltify-core:kspCommonMainKotlinMetadata")
}

tasks.withType<KotlinNativeCompile> {
    dependsOn(":saltify-core:kspCommonMainKotlinMetadata")
}

tasks.withType<KotlinJsCompile> {
    dependsOn(":saltify-core:kspCommonMainKotlinMetadata")
}

tasks.matching { it.name.lowercase().endsWith("sourcesjar") }.configureEach {
    dependsOn(":saltify-core:kspCommonMainKotlinMetadata")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = rootProject.group.toString(),
        artifactId = "saltify-core",
        version = rootProject.version.toString()
    )

    pom {
        name = "saltify-core"
        description = "Cross platform and extensible QQ Bot framework & Milky SDK"
        url = "https://github.com/SaltifyDev/saltify"
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
            connection = "scm:git:git://github.com/SaltifyDev/saltify.git"
            developerConnection = "scm:git:ssh://github.com/SaltifyDev/saltify.git"
            url = "https://github.com/SaltifyDev/saltify"
        }
    }
}
