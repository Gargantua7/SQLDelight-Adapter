package com.gargantua7.kotlin.ksp.sqldelight.adapter.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

fun Resolver.collectSerializer(): Map<KSClassDeclaration, KSClassDeclaration> {
    return getSymbolsWithAnnotation("com.gargantua7.kotlin.ksp.sqldelight.adapter.annotation.SQLDelightColumnSerializer")
        .filterIsInstance<KSClassDeclaration>()
        .onEach {
            check(it.classKind == ClassKind.OBJECT) {
                "Only objects can be annotated with @SQLDelightAdapter: ${it.qualifiedName?.asString()}"
            }
        }.associateBy { clazz ->
            clazz.superTypes.firstOrNull { type ->
                type.resolve().declaration.qualifiedName?.asString() == "kotlinx.serialization.KSerializer"
            } ?: error("Class ${clazz.qualifiedName?.asString()} must implement kotlinx.serialization.KSerializer")
        }.mapKeys { (type, _) ->
            type.resolve().arguments.first().type!!.resolve().declaration as KSClassDeclaration
        }
}
