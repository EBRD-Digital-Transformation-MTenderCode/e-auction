package com.procurement.auction.infrastructure.web.response.version.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.auction.infrastructure.web.response.version.ApiVersion2
import java.io.IOException

class ApiVersion2Deserializer : JsonDeserializer<ApiVersion2>() {
    companion object {
        fun deserialize(text: String) = ApiVersion2.orThrow(text) {
            IllegalAccessException("Invalid format of the api version. Expected: '${ApiVersion2.pattern}', actual: '$text'.")
        }
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ApiVersion2 =
        deserialize(
            jsonParser.text
        )
}
