package com.procurement.auction.domain.model.breakdown.status

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class BreakdownStatusDeserializer : ValueObjectDeserializer<BreakdownStatus>() {
    companion object {
        fun deserialize(text: String) = BreakdownStatus(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): BreakdownStatus =
        deserialize(
            jsonParser.text)
}