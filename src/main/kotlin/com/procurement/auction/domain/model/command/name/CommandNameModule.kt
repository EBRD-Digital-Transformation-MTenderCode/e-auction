package com.procurement.auction.domain.model.command.name

import com.fasterxml.jackson.databind.module.SimpleModule

class CommandNameModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(CommandName::class.java, CommandNameSerializer())
        addDeserializer(CommandName::class.java, CommandNameDeserializer())
    }
}
