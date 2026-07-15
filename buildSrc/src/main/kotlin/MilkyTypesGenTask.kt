package buildsrc.convention

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class MilkyTypesGenTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        val file = File(dir, "org/ntqqrev/milky/MilkyTypes.kt")
        file.parentFile.mkdirs()

        execOperations.exec {
            commandLine(
                "npx", "milkygen", "generate", "kotlin/kotlinx-serialization",
                "--version", version.get(),
                "--output", file.absolutePath
            )
        }
    }
}
