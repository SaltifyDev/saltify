package org.ntqqrev.milky.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

fun String.lowerFirstChar(): String {
    if (this.isEmpty()) return this
    return this[0].lowercaseChar() + this.substring(1)
}

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
                if (pkg != "org.ntqqrev.milky") {
                    it.appendLine("import org.ntqqrev.milky.*")
                    it.appendLine()
                }

                val apiEndpointsClazz = resolver.getClassDeclarationByName(
                    resolver.getKSNameFromString("org.ntqqrev.milky.ApiEndpoint")
                )!!
                apiEndpointsClazz.getSealedSubclasses().forEach { api ->
                    val endpointName = api.simpleName.asString()
                    val superType = api.superTypes.first().resolve()
                    val typeArgs = superType.arguments
                    if (typeArgs.size != 2) return@forEach
                    val inputStruct = typeArgs[0].type?.resolve()?.declaration ?: return@forEach
                    val outputStruct = typeArgs[1].type?.resolve()?.declaration ?: return@forEach

                    it.append("suspend fun $originalName.${endpointName.lowerFirstChar()}(")

                    // input parameters
                    if (inputStruct is KSClassDeclaration) {
                        val inputProperties = inputStruct.getAllProperties().toList().sortedWith { a, b ->
                            // compare nullability
                            when {
                                a.type.resolve().isMarkedNullable && !b.type.resolve().isMarkedNullable -> 1
                                !a.type.resolve().isMarkedNullable && b.type.resolve().isMarkedNullable -> -1
                                else -> 0 // preserve original order
                            }
                        }
                        inputProperties.forEachIndexed { index, property ->
                            it.appendLine()
                            val propName = property.simpleName.asString()
                            val propType = property.type.resolve().declaration.simpleName.asString()
                            it.append("    $propName: $propType")
                            val typeArguments = property.type.resolve().arguments
                            if (typeArguments.isNotEmpty()) {
                                it.append("<")
                                typeArguments.forEachIndexed { argIndex, typeArg ->
                                    val argType = typeArg.type?.resolve()?.declaration?.simpleName?.asString() ?: "*"
                                    it.append(argType)
                                    if (argIndex < typeArguments.size - 1) {
                                        it.append(", ")
                                    }
                                }
                                it.append(">")
                            }
                            if (property.type.resolve().isMarkedNullable) {
                                it.append("?")
                            }
                            if (index < inputProperties.size - 1) {
                                it.append(", ")
                            }
                        }
                        it.appendLine()
                    }
                    it.append(")")

                    // output type
                    if (outputStruct is KSClassDeclaration) {
                        it.append(": ${outputStruct.simpleName.asString()}")
                    }

                    it.appendLine(" {")

                    it.append("    ")
                    if (outputStruct is KSClassDeclaration) {
                        it.append("return ")
                    }
                    it.appendLine("this.callApi(")
                    it.appendLine("        endpoint = ApiEndpoint.$endpointName,")
                    if (inputStruct is KSClassDeclaration) {
                        it.appendLine("        param = ${inputStruct.simpleName.asString()}(")
                        val inputProperties = inputStruct.getAllProperties().toList()
                        inputProperties.forEachIndexed { index, property ->
                            val propName = property.simpleName.asString()
                            it.append("            $propName = $propName")
                            if (index < inputProperties.size - 1) {
                                it.appendLine(",")
                            } else {
                                it.appendLine()
                            }
                        }
                        it.appendLine("        ),")
                    }
                    it.appendLine("    )")

                    it.appendLine("}")
                    it.appendLine()
                }

                // Additional methods for sending messages
                it.appendLine(
                    """
                    suspend inline fun $originalName.sendPrivateMessage(
                        userId: Long,
                        block: MutableList<OutgoingSegment>.() -> Unit
                    ) = run {
                        val segments = mutableListOf<OutgoingSegment>().apply(block)
                        sendPrivateMessage(
                            userId = userId,
                            message = segments
                        )
                    }
                    
                    suspend inline fun $originalName.sendGroupMessage(
                        groupId: Long,
                        block: MutableList<OutgoingSegment>.() -> Unit
                    ) = run {
                        val segments = mutableListOf<OutgoingSegment>().apply(block)
                        sendGroupMessage(
                            groupId = groupId,
                            message = segments
                        )
                    }
                """.trimIndent()
                )

                // Additional methods for building segments
                val outgoingSegmentClazz = resolver.getClassDeclarationByName(
                    resolver.getKSNameFromString("org.ntqqrev.milky.OutgoingSegment")
                )!!
                outgoingSegmentClazz.getSealedSubclasses().forEach { segment ->
                    val segmentName = segment.simpleName.asString()
                    if (segmentName == "Forward") return@forEach
                    it.appendLine()
                    it.append("fun MutableList<OutgoingSegment>.${segmentName.lowerFirstChar()}(")

                    val segmentProperties = (segment.getAllProperties()
                        .first().type.resolve().declaration
                            as KSClassDeclaration)
                        .getAllProperties()
                        .toList()
                        .sortedWith { a, b ->
                            // compare nullability
                            when {
                                a.type.resolve().isMarkedNullable && !b.type.resolve().isMarkedNullable -> 1
                                !a.type.resolve().isMarkedNullable && b.type.resolve().isMarkedNullable -> -1
                                else -> 0 // preserve original order
                            }
                        }
                    segmentProperties.forEachIndexed { index, property ->
                        val propName = property.simpleName.asString()
                        val propType = property.type.resolve().declaration.simpleName.asString()
                        it.append("$propName: $propType")
                        if (property.type.resolve().isMarkedNullable) {
                            it.append("?")
                        }
                        if (index < segmentProperties.size - 1) {
                            it.append(", ")
                        }
                    }
                    it.appendLine(") = add(")
                    it.appendLine("    OutgoingSegment.$segmentName(")
                    it.appendLine("        data = OutgoingSegment.$segmentName.Data(")
                    segmentProperties.forEachIndexed { index, property ->
                        val propName = property.simpleName.asString()
                        it.append("                $propName = $propName")
                        if (index < segmentProperties.size - 1) {
                            it.appendLine(",")
                        } else {
                            it.appendLine()
                        }
                    }
                    it.appendLine("        )")
                    it.appendLine("    )")
                    it.appendLine(")")
                    it.appendLine()
                }

                it.appendLine("""
                    inline fun MutableList<OutgoingSegment>.forward(block: MutableList<OutgoingForwardedMessage>.() -> Unit) = add(
                        OutgoingSegment.Forward(
                            data = OutgoingSegment.Forward.Data(
                                messages = mutableListOf<OutgoingForwardedMessage>().apply(block)
                            )
                        )
                    )
                    
                    inline fun MutableList<OutgoingForwardedMessage>.node(
                        userId: Long,
                        senderName: String,
                        block: MutableList<OutgoingSegment>.() -> Unit
                    ) = add(
                        OutgoingForwardedMessage(
                            userId = userId,
                            senderName = senderName,
                            segments = mutableListOf<OutgoingSegment>().apply(block)
                        )
                    )
                """.trimIndent())

                // additional ext properties for incoming segments
                // access properties directly, crossing `data` layer
                it.appendLine()
                val incomingSegmentClazz = resolver.getClassDeclarationByName(
                    resolver.getKSNameFromString("org.ntqqrev.milky.IncomingSegment")
                )!!
                incomingSegmentClazz.getSealedSubclasses().forEach { segment ->
                    val segmentName = segment.simpleName.asString()
                    val segmentProperties = (segment.getAllProperties()
                        .first().type.resolve().declaration
                            as KSClassDeclaration)
                        .getAllProperties()
                    segmentProperties.forEach { prop ->
                        val propName = prop.simpleName.asString()
                        it.appendLine("val IncomingSegment.$segmentName.$propName")
                        it.appendLine("    get() = this.data.$propName")
                    }
                    it.appendLine()
                }
            }
        }

        return unableToProcess
    }
}