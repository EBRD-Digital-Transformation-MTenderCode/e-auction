package com.procurement.auction.domain.model.command.id

import com.fasterxml.jackson.databind.module.SimpleModule

class CommandIdModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(CommandId::class.java, CommandIdSerializer())
        addDeserializer(CommandId::class.java, CommandIdDeserializer())
    }
}
