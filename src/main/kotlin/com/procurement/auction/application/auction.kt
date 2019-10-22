package com.procurement.auction.application

import java.time.Duration
import java.time.LocalTime

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

fun countAuctions(
    auctionDuration: Duration,
    slots: List<Pair<LocalTime, LocalTime>>
): Long {
    return slots.asSequence()
        .map { slot ->
            countAuctionsBySlot(auctionDuration = auctionDuration, start = slot.first, end = slot.second)
        }
        .sum()
}

private fun countAuctionsBySlot(auctionDuration: Duration, start: LocalTime, end: LocalTime): Long {
    val slotDuration: Duration = slotDuration(start, end)
    return slotDuration.seconds / auctionDuration.seconds
}

private fun slotDuration(start: LocalTime, end: LocalTime) = Duration.between(start, end)