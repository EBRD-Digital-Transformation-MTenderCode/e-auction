package com.procurement.auction.domain.model.progressId

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class ProgressIdDeserializer : ValueObjectDeserializer<ProgressId>() {
    companion object {
        fun deserialize(text: String) = ProgressId(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ProgressId =
        ProgressIdDeserializer.deserialize(jsonParser.text)
}