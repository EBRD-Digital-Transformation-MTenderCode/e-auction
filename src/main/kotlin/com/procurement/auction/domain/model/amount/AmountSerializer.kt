package com.procurement.auction.domain.model.amount

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class AmountSerializer : ValueObjectSerializer<Amount>() {
    companion object {
        fun serialize(amount: Amount) = "%.2f".format(amount.value)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(amount: Amount, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeNumber(AmountSerializer.serialize(amount))
}