package org.ntqqrev.milky.ksp

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ApiExtensionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        ApiExtensionProcessor(
            codeGenerator = environment.codeGenerator,
        )
}