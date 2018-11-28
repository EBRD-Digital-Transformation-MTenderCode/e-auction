package com.procurement.auction.domain.model.lotId

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException
import java.util.*

class LotIdDeserializer : ValueObjectDeserializer<LotId>() {
    companion object {
        fun deserialize(text: String) = LotId(UUID.fromString(text))
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): LotId =
        LotIdDeserializer.deserialize(jsonParser.text)
}