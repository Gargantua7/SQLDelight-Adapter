package com.gargantua7.kotlin.ksp.sqldelight.adapter.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class SQLDelightAdapterProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return SQLDelightAdapterProcessor(
            environment.codeGenerator,
            environment.options["packages"] ?: "",
            environment.logger
        )
    }
}