package com.procurement.auction.domain.binding

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.ApiVersion
import java.io.IOException

class ApiVersionSerializer : JsonSerializer<ApiVersion>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(apiVersion: ApiVersion, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(apiVersion.toString())
    }
}