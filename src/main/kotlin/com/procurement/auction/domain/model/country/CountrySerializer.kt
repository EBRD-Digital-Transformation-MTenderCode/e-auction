package com.procurement.auction.domain.model.country

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class CountrySerializer : ValueObjectSerializer<Country>() {
    companion object {
        fun serialize(country: Country) = country.value
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(country: Country, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(CountrySerializer.serialize(country))
}