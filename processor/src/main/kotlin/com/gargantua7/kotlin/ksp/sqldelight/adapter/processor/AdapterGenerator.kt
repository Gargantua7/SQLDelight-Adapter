package com.gargantua7.kotlin.ksp.sqldelight.adapter.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

fun CodeGenerator.generationAdapter(
    serializers: Map<KSClassDeclaration, KSClassDeclaration>,
    adapters: Collection<AdapterClass>,
    packageName: String
) {
    createNewFile(Dependencies(true), packageName, "SQLDelightSerializers").writer().use { osw ->

        osw.write(
            """
                package $packageName
                
                import kotlinx.serialization.json.Json
                import kotlinx.serialization.json.JsonConfiguration
                import app.cash.sqldelight.ColumnAdapter
                import kotlinx.serialization.KSerializer
                import kotlinx.serialization.modules.SerializersModule
                import kotlinx.serialization.serializer
                
                private val sdSerializersModule = SerializersModule {
            """.trimIndent()
        )

        osw.write("\n")

        serializers.forEach { (clazz, adapter) ->
            osw.write("contextual(${clazz.qualifiedName!!.asString()}::class, ${adapter.qualifiedName!!.asString()})\n")
        }

        osw.write(
            """
                }
                
                private val sdJson = Json {
                    encodeDefaults = true
                    serializersModule = sdSerializersModule
                }
                
                private class CommonColumnAdapter<T : Any>(private val serializer: KSerializer<T>): ColumnAdapter<T, String> {

                    override fun encode(value: T): String {
                        return sdJson.encodeToString(serializer, value)
                    }

                    override fun decode(databaseValue: String): T {
                        return sdJson.decodeFromString(serializer, databaseValue)
                    }
                }

                private inline fun <reified T: Any> CommonColumnAdapter(): ColumnAdapter<T, String> {
                    return CommonColumnAdapter(sdSerializersModule.serializer<T>())
                }
            """.trimIndent()
        )

        adapters.forEach { adapter ->
            osw.write("\n\ninternal val ${adapter.model.simpleName.asString()}Adapter = ${adapter.raw.qualifiedName!!.asString()}(\n")

            adapter.properties.forEach { (property, type) ->

                val impl = if (type.classKind == ClassKind.ENUM_CLASS) "app.cash.sqldelight.EnumColumnAdapter()" else "CommonColumnAdapter()"

                osw.write("    ${property.simpleName.asString()} = $impl,\n")
            }

            osw.write(")")
        }

        osw.write("\n")
    }
}

data class AdapterClass(
    val raw: KSClassDeclaration,
    val model: KSClassDeclaration,
    val properties: Sequence<Pair<KSPropertyDeclaration, KSClassDeclaration>>,
)