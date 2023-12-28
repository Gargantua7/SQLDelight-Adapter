package com.gargantua7.kotlin.ksp.sqldelight.adapter.processor

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

private val exclusionPackages = listOf(
    "kotlin",
    "kotlin.collections",
)

fun Sequence<KSClassDeclaration>.checkSerializer(exclusion: Collection<String>) {

    forEach {
        check(
            it.qualifiedName?.asString() in exclusion ||
            it.classKind == ClassKind.ENUM_CLASS ||
            it.packageName.asString() in exclusionPackages ||
            it.annotations.any { annotation ->
                annotation.annotationType.resolve().declaration.qualifiedName?.asString() == "kotlinx.serialization.Serializable"
            }
        ) {
            "Class ${it.qualifiedName?.asString()} must be annotated with kotlinx.serialization.Serializable"
        }
    }
}