package com.gargantua7.kotlin.ksp.sqldelight.adapter.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import java.nio.charset.Charset

class SQLDelightAdapterProcessor(
    private val codeGenerator: CodeGenerator,
    private val inputPackageName: String,
    private val outputPackageName: String,
    private val logger: KSPLogger
): SymbolProcessor {

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {

        if (invoked) {
            return emptyList()
        }
        invoked = true

        val serializers = resolver.collectSerializer()
        val adapters = inputPackageName.split(",").flatMap { resolver.collectAdapter(it.trim()) }

        adapters.forEach {
            it.properties.map { it.second }.checkSerializer(serializers.keys.mapNotNull { it.qualifiedName?.asString() })
        }

        codeGenerator.generationAdapter(serializers, adapters, outputPackageName)

        return emptyList()
    }
}