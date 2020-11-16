package com.procurement.auction.infrastructure.web.response.version.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import java.io.IOException

class ApiVersion2Serializer : JsonSerializer<ApiVersion>() {
    companion object {
        fun serialize(apiVersion: ApiVersion): String = apiVersion.underlying
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(apiVersion: ApiVersion, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(serialize(apiVersion))
    }
}
