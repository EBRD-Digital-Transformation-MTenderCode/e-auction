package com.procurement.auction.domain.model.platformId

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException
import java.util.*

class PlatformIdDeserializer : ValueObjectDeserializer<PlatformId>() {
    companion object {
        fun deserialize(text: String) = PlatformId(UUID.fromString(text))
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): PlatformId =
        PlatformIdDeserializer.deserialize(jsonParser.text)
}