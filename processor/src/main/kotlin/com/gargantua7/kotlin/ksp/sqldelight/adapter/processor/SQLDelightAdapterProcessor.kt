package com.gargantua7.kotlin.ksp.sqldelight.adapter.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import java.nio.charset.Charset

class SQLDelightAdapterProcessor(
    private val codeGenerator: CodeGenerator,
    private val packageName: String,
    private val logger: KSPLogger
): SymbolProcessor {

    private var invoked = false

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {

        if (invoked) {
            return emptyList()
        }
        invoked = true

        initCommon()

        val annotated = resolver.getSymbolsWithAnnotation("com.gargantua7.kotlin.ksp.sqldelight.adapter.annotation.SQLDelightAdapter")
            .filterIsInstance<KSClassDeclaration>()
            .checkAdapter()
            .associate { it }

        resolver.getDeclarationsFromPackage(packageName)
            .mapNotNull { it as? KSClassDeclaration }
            .mapNotNull { symbol ->
                (symbol.declarations.firstOrNull { sub ->
                    sub is KSClassDeclaration &&
                            sub.simpleName.asString() == "Adapter" &&
                            sub.declarations
                                .filterIsInstance<KSPropertyDeclaration>()
                                .all {
                                    it.type.resolve().declaration.qualifiedName?.asString() == "app.cash.sqldelight.ColumnAdapter"
                                }
                } as? KSClassDeclaration)?.let { symbol to it }
            }.associateWith { (_, it) -> it.declarations.mapNotNull { child -> child as? KSPropertyDeclaration } }
            .onEach { (_, it) -> it.checkSerializable(annotated.keys) }
            .forEach { (it, properties) ->
                val (symbol, adapter) = it
                val className = "${symbol.simpleName.asString()}Adapter"
                val file = codeGenerator.createNewFile(Dependencies(true), packageName, className)

                file.writer(Charset.defaultCharset()).use { osw ->
                    osw.write("package $packageName\n\n")

                    adapter.qualifiedName?.let { qualifiedName ->
                        osw.write("import ${qualifiedName.asString()}\n\n")
                    }

                    osw.write("val $className = Adapter(\n")

                    properties.forEach {
                        val type = it.type.resolve()
                        val name = it.simpleName.asString()
                        val argument = type.arguments.first().type?.resolve()?.declaration as? KSClassDeclaration
                        val argumentName = argument?.qualifiedName?.asString()
                        val adapterTarget = when {
                            argumentName in annotated -> annotated[argumentName]!!
                            argument?.classKind == ClassKind.ENUM_CLASS -> "app.cash.sqldelight.EnumColumnAdapter()"
                            else -> "CommonColumnAdapter()"
                        }

                        osw.write("    $name = $adapterTarget,\n")
                    }
                    osw.write(")")
                }
            }

        return emptyList()
    }

    private fun initCommon() {
        val common = codeGenerator.createNewFile(Dependencies(true), packageName, "CommonColumnAdapter")
        common.writer(Charset.defaultCharset()).use { osw ->
            osw.write(
                """
                    package $packageName
                    
                    import app.cash.sqldelight.ColumnAdapter
                    import kotlinx.serialization.KSerializer
                    import kotlinx.serialization.json.Json
                    import kotlinx.serialization.serializer

                    class CommonColumnAdapter<T : Any>(private val serializer: KSerializer<T>): ColumnAdapter<T, String> {

                        override fun encode(value: T): String {
                            return Json.encodeToString(serializer, value)
                        }

                        override fun decode(databaseValue: String): T {
                            return Json.decodeFromString(serializer, databaseValue)
                        }
                    }

                    internal inline fun <reified T: Any> CommonColumnAdapter(): ColumnAdapter<T, String> {
                        return CommonColumnAdapter(serializer<T>())
                    }
                """.trimIndent()
            )
        }
    }

    private fun Sequence<KSClassDeclaration>.checkAdapter(): Sequence<Pair<String, String>> {
        return mapNotNull { symbol ->
            symbol.superTypes
                .map { it.resolve() }
                .firstOrNull { (it.declaration as? KSClassDeclaration)?.classKind == ClassKind.INTERFACE }
                ?.let { type ->
                    check(type.declaration.qualifiedName?.asString() == "app.cash.sqldelight.ColumnAdapter") {
                        "Class ${symbol.qualifiedName?.asString()} must implement app.cash.sqldelight.ColumnAdapter"
                    }

                    check(type.arguments.size == 2) {
                        "Class ${symbol.qualifiedName?.asString()} must implement app.cash.sqldelight.ColumnAdapter"
                    }

                    check(type.arguments[1].type!!.resolve().declaration.qualifiedName?.asString()!! == "kotlin.String") {
                        "Class ${symbol.qualifiedName?.asString()} must implement app.cash.sqldelight.ColumnAdapter"
                    }

                    type.arguments.first().type!!.resolve().declaration.qualifiedName!!.asString() to buildString {
                        append(symbol.qualifiedName!!.asString())

                        if (symbol.classKind == ClassKind.CLASS) {
                            append("()")
                        }
                    }
                }
        }
    }

    private fun Sequence<KSPropertyDeclaration>.checkSerializable(exclusion: Set<String>) {

        onEach { property ->
            property.type.resolve().arguments.forEach { argument ->
                val type = argument.type!!.resolve()
                if (type.declaration.qualifiedName!!.asString() !in exclusion) {
                    if (type.declaration.packageName.asString() != "kotlin") {
                        check(type.annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == "kotlinx.serialization.Serializable" }) {
                            "Property ${property.qualifiedName?.asString()} must be annotated with kotlinx.serialization.Serializable"
                        }
                    }
                }
            }
        }
    }

}