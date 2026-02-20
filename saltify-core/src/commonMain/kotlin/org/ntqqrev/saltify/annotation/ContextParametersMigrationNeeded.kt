package org.ntqqrev.saltify.annotation

/**
 * 由这个注解标注的定义会在将来 Context Parameters 稳定后迁移到 extension 软件包下。
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
internal annotation class ContextParametersMigrationNeeded
