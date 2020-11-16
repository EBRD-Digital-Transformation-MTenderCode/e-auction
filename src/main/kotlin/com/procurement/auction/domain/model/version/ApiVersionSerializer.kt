package com.procurement.auction.domain.model.version

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import java.io.IOException

class ApiVersionSerializer : ValueObjectSerializer<ApiVersion>() {
    companion object {
        fun serialize(apiVersion: ApiVersion) = apiVersion.underlying
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(apiVersion: ApiVersion, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(serialize(apiVersion))
}