package com.procurement.auction.domain.model.command.name

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class CommandNameSerializer : ValueObjectSerializer<CommandName>() {
    companion object {
        fun serialize(commandId: CommandName) = commandId.code
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(commandName: CommandName, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(serialize(commandName))
}