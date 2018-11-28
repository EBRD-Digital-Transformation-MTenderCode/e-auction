package com.procurement.auction.domain.model.auction.status

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class AuctionsStatusDeserializer : ValueObjectDeserializer<AuctionsStatus>() {
    companion object {
        fun deserialize(text: String) =
            AuctionsStatus.valueOfId(text.toInt())
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): AuctionsStatus =
        deserialize(jsonParser.text)
}