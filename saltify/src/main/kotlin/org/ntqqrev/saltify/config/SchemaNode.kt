package org.ntqqrev.saltify.config

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf

sealed class SchemaNode(
    val name: String,
    val description: String,
)

class ObjectSchemaNode(
    name: String,
    description: String,
    val properties: List<SchemaNode>,
) : SchemaNode(name, description) {
    companion object {
        fun create(
            name: String,
            description: String,
            clazz: KClass<*>
        ): ObjectSchemaNode {
            return ObjectSchemaNode(
                name,
                description,
                clazz.declaredMemberProperties
                    .map { property ->
                        val configurable = property.annotations.filterIsInstance<Configurable>().firstOrNull()
                            ?: throw IllegalArgumentException("Property ${property.name} is not annotated with @Configurable")
                        when (val type = property.returnType.classifier) {
                            String::class -> StringSchemaNode(property.name, configurable.description)
                            Int::class -> {
                                val intRange = property.annotations.filterIsInstance<IntRange>().firstOrNull<IntRange>()
                                IntegerSchemaNode(
                                    property.name,
                                    configurable.description,
                                    intRange?.min?.toLong(),
                                    intRange?.max?.toLong()
                                )
                            }

                            Double::class -> {
                                val doubleRange = property.annotations.filterIsInstance<DoubleRange>().firstOrNull()
                                DecimalSchemaNode(
                                    property.name,
                                    configurable.description,
                                    doubleRange?.min,
                                    doubleRange?.max
                                )
                            }

                            Long::class -> {
                                val longRange =
                                    property.annotations.filterIsInstance<LongRange>().firstOrNull<LongRange>()
                                IntegerSchemaNode(
                                    property.name,
                                    configurable.description,
                                    longRange?.min,
                                    longRange?.max
                                )
                            }

                            Float::class -> {
                                val floatRange = property.annotations.filterIsInstance<FloatRange>().firstOrNull()
                                DecimalSchemaNode(
                                    property.name,
                                    configurable.description,
                                    floatRange?.min?.toDouble(),
                                    floatRange?.max?.toDouble()
                                )
                            }

                            Boolean::class -> BooleanSchemaNode(property.name, configurable.description)
                            else -> {
                                if (type is KClass<*> && type.isSubclassOf(Enum::class)) {
                                    EnumSchemaNode(
                                        property.name,
                                        configurable.description,
                                        type.java.enumConstants
                                        .map {
                                            val key = it.toString()
                                            EnumSchemaNode.EnumValue(
                                                name = key,
                                                description = type.java.getField(key)
                                                    .getAnnotation(Option::class.java)
                                                    ?.description ?: key
                                            )
                                        }
                                    )
                                } else {
                                    create(
                                        name = property.name,
                                        description = configurable.description,
                                        clazz = type as? KClass<*>
                                            ?: throw IllegalArgumentException("Unsupported type: $type for property ${property.name}")
                                    )
                                }
                            }
                        }
                    }
            )
        }
    }
}

class StringSchemaNode(
    name: String,
    description: String,
) : SchemaNode(name, description)

class IntegerSchemaNode(
    name: String,
    description: String,
    val min: Long? = null,
    val max: Long? = null,
) : SchemaNode(name, description)

class DecimalSchemaNode(
    name: String,
    description: String,
    val min: Double? = null,
    val max: Double? = null,
) : SchemaNode(name, description)

class BooleanSchemaNode(
    name: String,
    description: String,
) : SchemaNode(name, description)

class EnumSchemaNode(
    name: String,
    description: String,
    val values: List<EnumValue>
) : SchemaNode(name, description) {
    class EnumValue(
        val name: String,
        val description: String,
    )
}