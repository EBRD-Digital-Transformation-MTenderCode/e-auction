package com.procurement.auction.domain.model.auction.status

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.auction.domain.model.ValueObject

enum class AuctionsStatus(val id: Int, val description: String) : ValueObject {
    SCHEDULED(1, "auctions are scheduled"),
    CANCELED(2, "auctions are cancelled"),
    STARTED(3, "auctions are started"),
    ENDED(4, "auctions are ended");

    companion object {
        private val map: Map<Int, AuctionsStatus> = mutableMapOf<Int, AuctionsStatus>().apply {
            enumValues<AuctionsStatus>().forEach { status ->
                this[status.id] = status
            }
        }

        fun valueOfId(id: Int): AuctionsStatus =
            map[id] ?: throw IllegalArgumentException("Status with id: '$id' not found.")
    }

    @JsonValue
    fun id(): Int = id
}