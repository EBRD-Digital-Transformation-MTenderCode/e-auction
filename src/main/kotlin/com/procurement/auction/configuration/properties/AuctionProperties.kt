package com.procurement.auction.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "auctions")
class AuctionProperties {
    var url: Url? = null
    var qtyRounds: Long? = 3
    var qtyParticipants: Int? = 4
    var durationOneStep: Duration? = Duration.ofSeconds(180)
    var durationPauseAfterStep: Duration? = Duration.ofSeconds(30)
    var durationPauseAfterAuction: Duration? = Duration.ofSeconds(30)
    val durationOneRound: Duration = durationOneStep!! + durationPauseAfterStep!!
    val durationOneAuction = durationAuction(qtyParticipants!!)

    fun durationAuction(qtyParticipants: Int): Duration =
        durationOneRound.multipliedBy(qtyParticipants.toLong())
            .multipliedBy(qtyRounds!!) + durationPauseAfterAuction

    data class Url(
        var protocol: String? = null,
        var host: String? = null
    )
}