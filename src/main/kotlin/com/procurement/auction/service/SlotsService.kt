package com.procurement.auction.service

import com.procurement.auction.configuration.properties.DefaultSchedulerProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.Country
import com.procurement.auction.domain.KeyOfSlot
import com.procurement.auction.domain.schedule.AuctionsDefinition
import com.procurement.auction.domain.schedule.SlotDefinition
import com.procurement.auction.domain.schedule.Slots
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
    fun booking(auctionsDetails: List<AuctionsDefinition.Detail>, slots: Slots): Map<SlotDefinition, List<AuctionsDefinition.Detail>>

    fun loadSlots(selectedDate: LocalDate, country: Country): Slots
    fun saveSlots(cpid: CPID, bookedSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult

    fun validateCountLots(auctionsDefinition: AuctionsDefinition)
}

@Service
class SlotsServiceImpl(schedulerProperties: SchedulerProperties,
                       private val slotsRepository: SlotsRepository) : SlotsService {

    private val defaultSchedulerProperties = DefaultSchedulerProperties(schedulerProperties)

    override fun validateCountLots(auctionsDefinition: AuctionsDefinition) {
        if (allocation(auctionsDefinition.details, defaultSchedulerProperties.slotsDefinitions).isEmpty())
            throw OutOfAuctionException()
    }

    override fun create(date: LocalDate, country: Country): Slots {
        return Slots(
            isNew = true,
            date = date,
            country = country,
            definitions = TreeSet<Slots.Definition>().apply {
                for (slotDefinition in defaultSchedulerProperties.slotsDefinitions) {
                    this.add(
                        Slots.Definition(
                            keyOfSlot = slotDefinition.keyOfSlot,
                            startTime = slotDefinition.startTime,
                            endTime = slotDefinition.endTime,
                            maxLines = slotDefinition.maxLines,
                            cpids = emptySet()
                        )
                    )
                }
            }
        )
    }

    override fun booking(auctionsDetails: List<AuctionsDefinition.Detail>,
                         slots: Slots): Map<SlotDefinition, List<AuctionsDefinition.Detail>> {
        val result = bookingInOneSlot(auctionsDetails, slots)
        return if (result.isNotEmpty()) result else bookingInSeveralSlots(auctionsDetails, slots)
    }

    private fun bookingInOneSlot(auctionsDetails: List<AuctionsDefinition.Detail>,
                                 slots: Slots): Map<SlotDefinition, List<AuctionsDefinition.Detail>> {
        for (slotDefinition in (slots.definitions).reversed()) {
            if (slotDefinition.hasAvailableLines()) {
                val durationAllAuctions = durationAllAuctions(auctionsDetails)
                if (slotDefinition.duration >= durationAllAuctions) return mapOf(slotDefinition to auctionsDetails)
            }
        }
        return emptyMap()
    }

    private fun durationAllAuctions(auctionsDetails: List<AuctionsDefinition.Detail>): Duration {
        var sum = Duration.ZERO
        for (auctionDetail in auctionsDetails) {
            sum += auctionDetail.duration
        }
        return sum
    }

    private fun bookingInSeveralSlots(auctionsDetails: List<AuctionsDefinition.Detail>,
                                      slots: Slots): Map<SlotDefinition, List<AuctionsDefinition.Detail>> =
        allocation(auctionsDetails, slotsDefinitions = slots.definitions)

    private fun allocation(auctionsDetails: List<AuctionsDefinition.Detail>,
                           slotsDefinitions: Set<SlotDefinition>): Map<SlotDefinition, List<AuctionsDefinition.Detail>> {
        var indexAuctionDetail = 0

        val auctionsBySlots = LinkedHashMap<SlotDefinition, List<AuctionsDefinition.Detail>>()
        for (slotDefinition in slotsDefinitions) {
            if (!slotDefinition.hasAvailableLines()) continue

            val auctionsDetailsInSlot = mutableListOf<AuctionsDefinition.Detail>()
            var slotDuration = slotDefinition.duration.seconds

            lotsLoop@
            while (true) {
                if (indexAuctionDetail < auctionsDetails.size) {
                    val auctionDetail = auctionsDetails[indexAuctionDetail]
                    val auctionDuration = auctionDetail.duration.seconds
                    if (auctionDuration <= slotDuration) {
                        auctionsDetailsInSlot.add(auctionDetail)
                        slotDuration -= auctionDuration
                        indexAuctionDetail++
                    } else
                        break@lotsLoop
                } else
                    break@lotsLoop
            }

            auctionsBySlots[slotDefinition] = auctionsDetailsInSlot
            if (indexAuctionDetail == auctionsDetails.size) return auctionsBySlots
        }

        return emptyMap()
    }

    override fun loadSlots(selectedDate: LocalDate, country: Country): Slots =
        slotsRepository.load(selectedDate, country) ?: create(selectedDate, country)

    override fun saveSlots(cpid: CPID, bookedSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult =
        slotsRepository.save(cpid = cpid, bookedSlots = bookedSlots, slots = slots)
}