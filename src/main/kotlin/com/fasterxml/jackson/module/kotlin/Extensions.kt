package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ValueDeserializer
import com.fasterxml.jackson.databind.ValueSerializer
import com.fasterxml.jackson.databind.json.JsonMapper
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL
import kotlin.reflect.KClass

fun kotlinModule(initializer: KotlinModule.Builder.() -> Unit = {}): KotlinModule {
    val builder = KotlinModule.Builder()
    builder.initializer()
    return builder.build()
}

fun jsonMapper(initializer: JsonMapper.Builder.() -> Unit = {}): JsonMapper {
    val builder = JsonMapper.builder()
    builder.initializer()
    return builder.build()
}

fun jacksonObjectMapper(): ObjectMapper = jsonMapper { addModule(kotlinModule()) }
fun jacksonMapperBuilder(): JsonMapper.Builder = JsonMapper.builder().addModule(kotlinModule())

// 22-Jul-2019, tatu: Can not be implemented same way as in 2.x, addition via mapper.builder():
//fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())

inline fun <reified T> jacksonTypeRef(): TypeReference<T> = object : TypeReference<T>() {}

inline fun <reified T> ObjectMapper.readValue(p: JsonParser): T = readValue(p, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValues(p: JsonParser): MappingIterator<T> = readValues(p, jacksonTypeRef<T>())

inline fun <reified T> ObjectMapper.readValue(src: File): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: URL): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(content: String): T = readValue(content, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: Reader): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: InputStream): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: ByteArray): T = readValue(src, jacksonTypeRef<T>())

inline fun <reified T> ObjectMapper.treeToValue(n: TreeNode): T? = treeToValue(n, T::class.java)
inline fun <reified T> ObjectMapper.convertValue(from: Any): T = convertValue(from, jacksonTypeRef<T>())

inline fun <reified T> ObjectReader.readValuesTyped(p: JsonParser): Iterator<T> = readValues(p, jacksonTypeRef<T>())
inline fun <reified T> ObjectReader.treeToValue(n: TreeNode): T? = treeToValue(n, T::class.java)

internal fun DatabindException.wrapWithPath(refFrom: Any?, refFieldName: String) = DatabindException.wrapWithPath(this, refFrom, refFieldName)
internal fun DatabindException.wrapWithPath(refFrom: Any?, index: Int) = DatabindException.wrapWithPath(this, refFrom, index)

inline fun <reified T : Any> SimpleModule.addSerializer(kClass: KClass<T>, serializer: ValueSerializer<T>) = this.apply {
    addSerializer(kClass.java, serializer)
    addSerializer(kClass.javaObjectType, serializer)
}

inline fun <reified T : Any> SimpleModule.addDeserializer(kClass: KClass<T>, deserializer: ValueDeserializer<T>) = this.apply {
    addDeserializer(kClass.java, deserializer)
    addDeserializer(kClass.javaObjectType, deserializer)
}
