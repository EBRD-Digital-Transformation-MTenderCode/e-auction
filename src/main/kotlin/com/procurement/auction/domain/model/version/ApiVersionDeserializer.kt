package com.procurement.auction.domain.model.version

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class ApiVersionDeserializer : ValueObjectDeserializer<ApiVersion>() {
    companion object {
        fun deserialize(text: String) = ApiVersion.valueOf(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ApiVersion =
        ApiVersionDeserializer.deserialize(jsonParser.text)
}