package com.procurement.auction.domain.model.currency

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class CurrencyDeserializer : ValueObjectDeserializer<Currency>() {
    companion object {
        fun deserialize(text: String) = Currency(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Currency =
        CurrencyDeserializer.deserialize(jsonParser.text)
}