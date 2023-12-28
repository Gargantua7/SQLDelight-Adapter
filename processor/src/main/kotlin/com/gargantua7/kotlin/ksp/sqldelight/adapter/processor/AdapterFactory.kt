package com.gargantua7.kotlin.ksp.sqldelight.adapter.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

@OptIn(KspExperimental::class)
fun Resolver.collectAdapter(
    packageName: String
): Sequence<AdapterClass> {

    return getDeclarationsFromPackage(packageName)
        .filterIsInstance<KSClassDeclaration>()
        .mapNotNull { clazz ->
            clazz.convertToAdapterClass()
        }
}

private fun KSClassDeclaration.convertToAdapterClass(): AdapterClass? {
    val adapter = declarations
        .filterIsInstance<KSClassDeclaration>()
        .firstOrNull { sub ->
            sub.simpleName.asString() == "Adapter" &&
                sub.declarations
                    .filterIsInstance<KSPropertyDeclaration>()
                    .all {
                        it.type.resolve().declaration.qualifiedName?.asString() == "app.cash.sqldelight.ColumnAdapter"
                    }
        } ?: return null

    val properties = adapter.declarations
        .filterIsInstance<KSPropertyDeclaration>()
        .map { property ->
            property to property.type.resolve().arguments.first().type!!.resolve().declaration as KSClassDeclaration
        }

    return AdapterClass(adapter, this, properties)
}