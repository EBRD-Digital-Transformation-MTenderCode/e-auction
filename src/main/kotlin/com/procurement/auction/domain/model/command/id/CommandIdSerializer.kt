package com.procurement.auction.domain.model.command.id

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class CommandIdSerializer : ValueObjectSerializer<CommandId>() {
    companion object {
        fun serialize(commandId: CommandId) = commandId.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(commandId: CommandId, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(serialize(
            commandId))
}