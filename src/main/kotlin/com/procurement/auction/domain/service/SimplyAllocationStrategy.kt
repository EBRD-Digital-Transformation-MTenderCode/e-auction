package com.procurement.auction.domain.service

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.auction.EstimatedDurationAuction
import com.procurement.auction.domain.model.bucket.AuctionsTimes
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.slots.Slot
import com.procurement.auction.domain.model.slots.id.SlotId
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class SimplyAllocationStrategy : AllocationStrategy {

    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun allocation(startDate: LocalDate,
                            estimates: List<EstimatedDurationAuction>,
                            slots: Collection<Slot>): AuctionsTimes? {
        log.debug { "Attempt to allocate lots: '$estimates' by slots: '$slots'." }
        val oneSlot = allocationInOneSlot(startDate, estimates, slots)
        return if (oneSlot != null) {
            log.debug { "Attempt allocate in one slot is success." }
            oneSlot
        } else {
            allocationInMultiSlots(startDate, estimates, slots)?.also {
                log.debug { "Attempt allocate in multiple slots is success." }
            }
        }
    }

    private fun allocationInOneSlot(startDate: LocalDate,
                                    estimates: List<EstimatedDurationAuction>,
                                    slots: Collection<Slot>): AuctionsTimes? {
        log.debug { "Attempt allocate in one slot." }
        val orderedSlots = slots.sortedBy { it.duration }
        for (slot in orderedSlots) {
            if (slot.isAvailable) {
                val durationAllAuctions = durationAllAuctions(estimates)
                if (slot.duration >= durationAllAuctions) {
                    val items = mutableMapOf<LotId, LocalDateTime>()
                    var startTime = slot.startTime
                    estimates.forEach {
                        val lotId = it.lotId
                        items[lotId] = LocalDateTime.of(startDate, startTime)
                        startTime = startTime.plus(it.duration)
                    }
                    return AuctionsTimes(
                        startDateTime = LocalDateTime.of(startDate, slot.startTime),
                        slotsIds = setOf(slot.id),
                        items = items
                    )
                }
            }
        }
        return null
    }

    private fun durationAllAuctions(estimates: List<EstimatedDurationAuction>): Duration {
        var sum = Duration.ZERO
        for (estimate in estimates) {
            sum += estimate.duration
        }
        return sum
    }

    private fun allocationInMultiSlots(startDate: LocalDate,
                                       estimates: List<EstimatedDurationAuction>,
                                       slots: Collection<Slot>): AuctionsTimes? {
        log.debug { "Attempt allocate in multiple slots." }
        val orderedSlots = slots.sortedByDescending { it.duration }
        val slotsIds = mutableSetOf<SlotId>()
        val timesStartAuctionByLotId = mutableMapOf<LotId, LocalDateTime>()
        var indexLot = 0
        var minStartTime: LocalTime? = null

        slotsLoop@
        for (slot in orderedSlots) {
            if (!slot.isAvailable) continue

            var slotTimeRemaining = slot.duration
            var startTime = slot.startTime

            if (minStartTime == null || minStartTime.isAfter(startTime))
                minStartTime = startTime

            lotsLoop@
            while (true) {
                if (indexLot < estimates.size) {
                    val estimate = estimates[indexLot]
                    if (estimate.duration <= slotTimeRemaining) {
                        slotsIds.add(slot.id)
                        timesStartAuctionByLotId[estimate.lotId] = LocalDateTime.of(startDate, startTime)
                        indexLot++
                        slotTimeRemaining -= estimate.duration
                        startTime = startTime.plus(estimate.duration)
                    } else
                        continue@slotsLoop
                } else
                    break@lotsLoop
            }

            if (estimates.size == timesStartAuctionByLotId.size)
                return AuctionsTimes(
                    startDateTime = LocalDateTime.of(startDate, minStartTime),
                    slotsIds = slotsIds,
                    items = timesStartAuctionByLotId
                )
        }

        return null
    }
}
