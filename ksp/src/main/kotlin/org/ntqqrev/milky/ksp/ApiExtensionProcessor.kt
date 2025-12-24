package org.ntqqrev.milky.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

class ApiExtensionProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("org.ntqqrev.milky.WithApiExtension")
        val unableToProcess = mutableListOf<KSAnnotated>()
        symbols.forEach { symbol ->
            if (!symbol.validate()) {
                unableToProcess.add(symbol)
            }
            val clazz = symbol as? KSClassDeclaration ?: return@forEach
            val pkg = clazz.packageName.asString()
            val originalName = clazz.simpleName.asString()
            val genName = "${originalName}ApiExtension"

            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(
                    aggregating = false,
                    sources = clazz.containingFile?.let { arrayOf(it) } ?: arrayOf()
                ),
                packageName = pkg,
                fileName = genName,
                extensionName = "kt"
            )

            file.bufferedWriter().use {
                it.appendLine("package $pkg")
                it.appendLine()
                // TODO: write extensions
            }
        }

        return unableToProcess
    }
}