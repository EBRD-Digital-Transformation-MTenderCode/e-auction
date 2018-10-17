package com.procurement.auction.domain.request

import com.fasterxml.jackson.annotation.JsonValue

enum class Command(val code: String, val id: Int) {
    SCHEDULE("scheduleAuctions", 1);

    companion object {
        val map: Map<Int, Command> = mutableMapOf<Int, Command>().apply {
            enumValues<Command>().forEach { command ->
                this[command.id] = command
            }
        }
    }

    @JsonValue
    fun value(): String = code

    override fun toString(): String = code

    fun valueOfId(id: Int): Command = map[id] ?: throw IllegalArgumentException("Command with id: '$id' not found.")
}