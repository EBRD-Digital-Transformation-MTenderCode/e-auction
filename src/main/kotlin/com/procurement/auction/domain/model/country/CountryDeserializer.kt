package com.procurement.auction.domain.model.country

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class CountryDeserializer : ValueObjectDeserializer<Country>() {
    companion object {
        fun deserialize(text: String) = Country(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Country =
        CountryDeserializer.deserialize(jsonParser.text)
}