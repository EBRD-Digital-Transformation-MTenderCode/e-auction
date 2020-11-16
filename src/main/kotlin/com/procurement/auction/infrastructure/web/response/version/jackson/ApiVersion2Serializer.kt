package com.procurement.auction.infrastructure.web.response.version.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.infrastructure.web.response.version.ApiVersion2
import java.io.IOException

class ApiVersion2Serializer : JsonSerializer<ApiVersion2>() {
    companion object {
        fun serialize(apiVersion: ApiVersion2): String = apiVersion.underlying
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(apiVersion: ApiVersion2, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(serialize(apiVersion))
    }
}
