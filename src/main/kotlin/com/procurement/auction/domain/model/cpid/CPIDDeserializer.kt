package com.procurement.auction.domain.model.cpid

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class CPIDDeserializer : ValueObjectDeserializer<CPID>() {
    companion object {
        fun deserialize(text: String) = CPID(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): CPID =
        CPIDDeserializer.deserialize(jsonParser.text)
}