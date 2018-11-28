package com.procurement.auction.domain.model.currency

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class CurrencySerializer : ValueObjectSerializer<Currency>() {
    companion object {
        fun serialize(currency: Currency) = currency.value
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(currency: Currency, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(CurrencySerializer.serialize(currency))
}