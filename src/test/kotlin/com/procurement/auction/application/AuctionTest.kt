package com.procurement.auction.application

import com.procurement.auction.domain.model.date.JsonTimeDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

class AuctionTest {
    companion object {
        private val AUCTION_DURATION = Duration.ofSeconds(2550)
    }

    @Test
    fun auctionDurationTest() {
        val actualAuctionDuration = auctionDuration(
            durationOneStep = Duration.ofSeconds(180),
            durationPauseAfterStep = Duration.ofSeconds(30),
            qtyParticipants = 4,
            qtyRounds = 3,
            durationPauseAfterAuction = Duration.ofSeconds(30)
        )

        assertEquals(AUCTION_DURATION, actualAuctionDuration)
    }

    @Test
    fun countAuctionsTest() {
        val slots = listOf(
            "06:00Z".toLocalTime() to "13:00Z".toLocalTime(),
            "07:00Z".toLocalTime() to "13:00Z".toLocalTime(),
            "08:00Z".toLocalTime() to "13:00Z".toLocalTime(),
            "09:00Z".toLocalTime() to "13:00Z".toLocalTime(),
            "10:00Z".toLocalTime() to "13:00Z".toLocalTime(),
            "11:00Z".toLocalTime() to "13:00Z".toLocalTime(),
            "12:00Z".toLocalTime() to "13:00Z".toLocalTime()
        )
        val actualAuctionDuration = countAuctions(
            auctionDuration = AUCTION_DURATION,
            slots = slots
        )

        assertEquals(36, actualAuctionDuration)
    }

    private fun String.toLocalTime(): LocalTime = JsonTimeDeserializer.deserialize(this)
}