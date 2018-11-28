package com.procurement.auction.domain.model.amount

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import com.procurement.auction.exception.app.AmountValueException
import java.io.IOException
import java.math.BigDecimal

class AmountDeserializer : ValueObjectDeserializer<Amount>() {
    companion object {
        fun deserialize(text: String): Amount {
            return try {
                val value = BigDecimal(text)
                Amount(value)
            } catch (exception: Exception) {
                throw AmountValueException(text, exception.message)
            }
        }
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Amount =
        AmountDeserializer.deserialize(jsonParser.text)
}