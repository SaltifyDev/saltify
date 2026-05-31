import buildsrc.convention.MilkyTypesGenTask

plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

val generateMilkyTypes by tasks.registering(MilkyTypesGenTask::class) {
    description = "Fetch milky types"
    outputDir = layout.buildDirectory.dir("./generated/source/milky/commonMain/kotlin")
    version = rootProject.version.toString()
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generateMilkyTypes.map { it.outputDir })

            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
