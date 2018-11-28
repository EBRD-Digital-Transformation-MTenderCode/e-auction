package com.procurement.auction.domain.model.operationId

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class OperationIdSerializer : ValueObjectSerializer<OperationId>() {
    companion object {
        fun serialize(operationId: OperationId) = operationId.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(operationId: OperationId, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(OperationIdSerializer.serialize(operationId))
}