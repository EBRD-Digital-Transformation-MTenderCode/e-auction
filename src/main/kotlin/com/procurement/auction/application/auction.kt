package com.procurement.auction.application

import java.time.Duration

fun auctionDuration(
    durationOneStep: Duration,
    durationPauseAfterStep: Duration,
    qtyParticipants: Long,
    qtyRounds: Long,
    durationPauseAfterAuction: Duration
): Duration {
    val durationOneStepWithPause = (durationOneStep + durationPauseAfterStep)
    val durationOneRound = durationOneStepWithPause.multipliedBy(qtyParticipants)
    val durationAllRounds = durationOneRound.multipliedBy(qtyRounds)
    return durationAllRounds + durationPauseAfterAuction
}
