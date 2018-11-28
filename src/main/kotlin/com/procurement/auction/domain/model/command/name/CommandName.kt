package com.procurement.auction.domain.model.command.name

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.auction.domain.model.ValueObject

enum class CommandName(val code: String) : ValueObject {
    SCHEDULE("scheduleAuctions"),
    AUCTIONS_START("auctionsStart"),
    AUCTIONS_END("auctionsEnd"),
    AUCTION_CANCEL("auctionsCancellation");

    companion object {
        val map: Map<String, CommandName> = mutableMapOf<String, CommandName>().apply {
            enumValues<CommandName>().forEach { commandName ->
                this[commandName.code] = commandName
            }
        }

        fun valueOfCode(code: String): CommandName =
            map[code] ?: throw IllegalArgumentException("The command with code: '$code' not found.")
    }

    @JsonValue
    fun value(): String = code

    override fun toString(): String = code
}