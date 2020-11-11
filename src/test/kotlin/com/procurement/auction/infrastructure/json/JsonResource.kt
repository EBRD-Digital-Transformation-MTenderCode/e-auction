package com.procurement.auction.infrastructure.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.cpid.CpidDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeModule
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.domain.model.ocid.OcidDeserializer
import com.procurement.auction.infrastructure.json.exception.JsonBindingException
import com.procurement.auction.infrastructure.json.exception.JsonFileNotFoundException
import com.procurement.auction.infrastructure.json.exception.JsonMappingException
import com.procurement.auction.infrastructure.json.exception.JsonParsingException
import com.procurement.auction.infrastructure.web.response.version.jackson.ApiVersion2Module
import java.io.IOException
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

typealias JSON = String

fun loadJson(fileName: String): JSON {
    return ClassPathResource.getFilePath(fileName)?.let { pathToFile ->
        val path = Paths.get(pathToFile)
        val buffer = Files.readAllBytes(path)
        String(buffer, Charset.defaultCharset())
    } ?: throw JsonFileNotFoundException("Error loading JSON. File by path: $fileName not found.")
}

fun JSON.compact(): JSON {
    val factory = JsonFactory()
    val parser = factory.createParser(this)
    val out = StringWriter()
    factory.createGenerator(out).use { gen ->
        while (parser.nextToken() != null) {
            gen.copyCurrentEvent(parser)
        }
    }
    return out.buffer.toString()
}

inline fun <reified T : Any> JsonNode.toObject(): T = try {
    JsonMapper.mapper.treeToValue(this, T::class.java)
} catch (exception: IOException) {
    val className = this::class.java.canonicalName
    throw JsonBindingException("Error binding JSON to an object of type '$className'.", exception)
}

inline fun <reified T : Any> JSON.toObject(): T = this.toObject(T::class.java)

fun <T : Any> JSON.toObject(target: Class<T>): T = try {
    JsonMapper.mapper.readValue(this, target)
} catch (exception: Exception) {
    throw JsonBindingException("Error binding JSON to an object of type '${target.canonicalName}'.", exception)
}

fun <T : Any> T.toJson(): JSON = try {
    JsonMapper.mapper.writeValueAsString(this)
} catch (exception: JsonProcessingException) {
    val className = this::class.java.canonicalName
    throw JsonMappingException("Error mapping an object of type '$className' to JSON.", exception)
}

fun JSON.toNode(): JsonNode = try {
    JsonMapper.mapper.readTree(this)
} catch (exception: JsonProcessingException) {
    throw JsonParsingException("Error parsing JSON to JsonNode.", exception)
}

private object ClassPathResource {
    fun getFilePath(fileName: String): String? = javaClass.classLoader.getResource(fileName)?.path
}

object JsonMapper {
    val mapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JsonDateTimeModule())
        registerModule(ApiVersion2Module())
        registerModule(getModule())

        configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
        configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private fun getModule(): SimpleModule {
        val simpleModule = SimpleModule()
        simpleModule.addDeserializer(Cpid::class.java, CpidDeserializer())
        simpleModule.addDeserializer(Ocid::class.java, OcidDeserializer())
        return simpleModule
    }
}