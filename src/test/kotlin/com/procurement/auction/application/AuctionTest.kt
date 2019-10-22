package com.procurement.auction.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class AuctionTest {

    @Test
    fun auctionDurationTest() {
        val actualAuctionDuration = auctionDuration(
            durationOneStep = Duration.ofSeconds(180),
            durationPauseAfterStep = Duration.ofSeconds(30),
            qtyParticipants = 4,
            qtyRounds = 3,
            durationPauseAfterAuction = Duration.ofSeconds(30)
        )

        assertEquals(Duration.ofSeconds(2550), actualAuctionDuration)
    }
}