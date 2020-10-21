package com.procurement.auction.infrastructure.service.command.type

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.auction.domain.model.enum.EnumElementProvider

enum class Command2Type(@JsonValue override val key: String) : Action, EnumElementProvider.Key {

    TODO("TODO");

    override fun toString(): String = key

    companion object : EnumElementProvider<Command2Type>(info = info()) {
        @JvmStatic
        @JsonCreator
        fun creator(name: String) = Command2Type.orThrow(name)
    }
}




