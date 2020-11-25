package com.procurement.auction.domain.model.operationId

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class OperationIdDeserializer : ValueObjectDeserializer<OperationId>() {
    companion object {
        fun deserialize(text: String) = OperationId.tryCreateOrNull(text)
            ?: throw ParseOperationIdException(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): OperationId =
        deserialize(jsonParser.text)
}