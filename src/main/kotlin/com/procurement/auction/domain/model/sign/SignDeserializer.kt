package com.procurement.auction.domain.model.sign

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException
import java.util.*

class SignDeserializer : ValueObjectDeserializer<Sign>() {
    companion object {
        fun deserialize(text: String) = Sign(UUID.fromString(text))
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Sign =
        SignDeserializer.deserialize(jsonParser.text)
}