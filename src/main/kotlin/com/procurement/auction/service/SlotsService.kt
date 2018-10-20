package com.procurement.auction.service

import com.procurement.auction.configuration.properties.DefaultSchedulerProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.Country
import com.procurement.auction.domain.KeyOfSlot
import com.procurement.auction.domain.LotsInfo
import com.procurement.auction.domain.SlotDefinition
import com.procurement.auction.domain.Slots
import com.procurement.auction.exception.OutOfAuctionException
import com.procurement.auction.repository.SlotsRepository
import com.procurement.auction.repository.SlotsSaveResult
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.collections.LinkedHashMap

class DefaultSlotDefinition(override val keyOfSlot: KeyOfSlot,
                            override val startTime: LocalTime,
                            override val endTime: LocalTime,
                            override val maxLines: Int) : SlotDefinition, Comparable<SlotDefinition> {
    override val duration: Duration = Duration.between(startTime, endTime)

    override fun compareTo(other: SlotDefinition): Int = startTime.compareTo(other.startTime)
}

interface SlotsService {
    fun create(date: LocalDate, country: Country): Slots
    fun booking(lotsDetails: List<LotsInfo.Detail>, slots: Slots): Map<SlotDefinition, List<LotsInfo.Detail>>

    fun loadSlots(selectedDate: LocalDate, country: Country): Slots
    fun saveSlots(cpid: CPID, bookedSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult

    fun validateCountLots(lotsInfo: LotsInfo)
}

@Service
class SlotsServiceImpl(schedulerProperties: SchedulerProperties,
                       private val slotsRepository: SlotsRepository) : SlotsService {

    private val defaultSchedulerProperties = DefaultSchedulerProperties(schedulerProperties)

    override fun validateCountLots(lotsInfo: LotsInfo) {
        if (allocation(lotsInfo.details, defaultSchedulerProperties.slotsDefinitions).isEmpty())
            throw OutOfAuctionException()
    }

    override fun create(date: LocalDate, country: Country): Slots {
        return Slots(
            isNew = true,
            date = date,
            country = country,
            definitions = TreeSet<Slots.Definition>().apply {
                for (slotDetail in defaultSchedulerProperties.slotsDefinitions) {
                    this.add(
                        Slots.Definition(
                            keyOfSlot = slotDetail.keyOfSlot,
                            startTime = slotDetail.startTime,
                            endTime = slotDetail.endTime,
                            maxLines = slotDetail.maxLines,
                            cpids = emptySet()
                        )
                    )
                }
            }
        )
    }

    override fun booking(lotsDetails: List<LotsInfo.Detail>, slots: Slots): Map<SlotDefinition, List<LotsInfo.Detail>> {
        val result = bookingInOneSlot(lotsDetails, slots)
        return if (result.isNotEmpty()) result else bookingInSeveralSlots(lotsDetails, slots)
    }

    private fun bookingInOneSlot(lotsDetails: List<LotsInfo.Detail>,
                                 slots: Slots): Map<SlotDefinition, List<LotsInfo.Detail>> {
        for (slotDetail in (slots.definitions).reversed()) {
            if (slotDetail.hasAvailableLines()) {
                val durationAllAuctions = durationAllAuctions(lotsDetails)
                if (slotDetail.duration >= durationAllAuctions) return mapOf(slotDetail to lotsDetails)
            }
        }
        return emptyMap()
    }

    private fun durationAllAuctions(lotsDetails: List<LotsInfo.Detail>): Duration {
        var sum = Duration.ZERO
        for (lot in lotsDetails) {
            sum += lot.duration
        }
        return sum
    }

    private fun bookingInSeveralSlots(lotsDetails: List<LotsInfo.Detail>,
                                      slots: Slots): Map<SlotDefinition, List<LotsInfo.Detail>> =
        allocation(lotsDetails, slotsDefinitions = slots.definitions)

    private fun allocation(lotsDetails: List<LotsInfo.Detail>,
                           slotsDefinitions: Set<SlotDefinition>): Map<SlotDefinition, List<LotsInfo.Detail>> {
        var indexLot = 0

        val result = LinkedHashMap<SlotDefinition, List<LotsInfo.Detail>>()
        for (slotDetail in slotsDefinitions) {
            if (!slotDetail.hasAvailableLines()) continue

            val setRelatedLots = mutableListOf<LotsInfo.Detail>()
            var slotDuration = slotDetail.duration.seconds

            lotsLoop@
            while (true) {
                if (indexLot < lotsDetails.size) {
                    val lot = lotsDetails[indexLot]
                    val auctionDuration = lot.duration.seconds
                    if (auctionDuration <= slotDuration) {
                        setRelatedLots.add(lot)
                        slotDuration -= auctionDuration
                        indexLot++
                    } else
                        break@lotsLoop
                } else
                    break@lotsLoop
            }

            result[slotDetail] = setRelatedLots
            if (indexLot == lotsDetails.size) return result
        }

        return emptyMap()
    }

    override fun loadSlots(selectedDate: LocalDate, country: Country): Slots =
        slotsRepository.load(selectedDate, country) ?: create(selectedDate, country)

    override fun saveSlots(cpid: CPID, bookedSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult =
        slotsRepository.save(cpid = cpid, bookedSlots = bookedSlots, slots = slots)
}