package com.procurement.auction.domain.binding

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.auction.domain.ApiVersion
import java.io.IOException

class ApiVersionDeserializer : JsonDeserializer<ApiVersion>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ApiVersion {
        val apiVersion = jsonParser.text
        return ApiVersion.valueOf(apiVersion)
    }
}