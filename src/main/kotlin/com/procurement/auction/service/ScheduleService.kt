package com.procurement.auction.service

import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.domain.LotsInfo
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.SlotDefinition
import com.procurement.auction.domain.request.schedule.ScheduleRQ
import com.procurement.auction.domain.schedule.PlannedAuction
import com.procurement.auction.exception.CalendarNoDataException
import com.procurement.auction.exception.NoLotsForAuctionPlanningException
import com.procurement.auction.repository.CalendarRepository
import com.procurement.auction.repository.PlannedAuctionKey
import com.procurement.auction.repository.PlannedAuctionRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

interface ScheduleService {
    fun schedule(request: ScheduleRQ): PlannedAuction
}

@Service
class ScheduleServiceImpl(private val auctionProperties: AuctionProperties,
                          private val plannedAuctionRepository: PlannedAuctionRepository,
                          private val calendarRepository: CalendarRepository,
                          private val slotsService: SlotsService) :
    ScheduleService {

    companion object {
        private const val dateOffsetDays = 1L
    }

    override fun schedule(request: ScheduleRQ): PlannedAuction {
        if (request.data.electronicAuctions.details.isEmpty())
            throw NoLotsForAuctionPlanningException()

        val plannedAuctions = loadPlannedAuctions(request.context.cpid, request.context.operationId)
        if (plannedAuctions != null) {
            return plannedAuctions
        }

        val lotsInfo: LotsInfo = genLotsInfo(request)
        slotsService.validateCountLots(lotsInfo)

        val country = lotsInfo.country

        var dateStartAuction = minDateOfStartAuction(lotsInfo.tenderPeriodEnd)
        while (true) {
            val selectedDate = getWorkDayByCalendar(country, dateStartAuction)
            val slots = slotsService.loadSlots(selectedDate, country)
            val booked = slotsService.booking(lotsInfo.details, slots)
            if (booked.isNotEmpty()) {
                val newPlannedAuctions =
                    genPlannedAuctions(startDate = selectedDate, lotsInfo = lotsInfo, booked = booked)
                val actualPlannedAuctions = savePlannedAuctions(request, newPlannedAuctions)
                if (newPlannedAuctions != actualPlannedAuctions) {
                    return actualPlannedAuctions
                }

                val cpid = request.context.cpid
                slotsService.saveSlots(cpid = cpid,
                    bookedSlots = booked.keys.asSequence().map { it.keyOfSlot }.toSet(),
                    slots = slots)
                return newPlannedAuctions
            }

            dateStartAuction = dateStartAuction.plusDays(1)
        }
    }

    private fun loadPlannedAuctions(cpid: String, operationId: UUID): PlannedAuction? {
        return plannedAuctionRepository.load(cpid, operationId)
    }

    private fun genLotsInfo(request: ScheduleRQ) = request.let {
        val context = it.context
        val cpid = context.cpid
        val country = context.country
        val data = it.data

        LotsInfo(
            cpid = cpid,
            country = country,
            tenderPeriodEnd = data.tenderPeriod.endDate.toLocalDate(),
            details = mutableListOf<LotsInfo.Detail>().apply {
                for (detail in data.electronicAuctions.details) {
                    val eligibleMinimumDifference =
                        detail.electronicAuctionModalities[0].eligibleMinimumDifference

                    add(
                        LotsInfo.Detail(
                            relatedLot = detail.relatedLot,
                            duration = auctionProperties.durationOneAuction,
                            electronicAuctionModalities = detail.electronicAuctionModalities.map {
                                LotsInfo.Detail.ElectronicAuctionModality(
                                    eligibleMinimumDifference = LotsInfo.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                        amount = eligibleMinimumDifference.amount,
                                        currency = eligibleMinimumDifference.currency
                                    )
                                )
                            }

                        )
                    )
                }
            }
        )
    }

    fun minDateOfStartAuction(endDate: LocalDate): LocalDate = endDate.plusDays(dateOffsetDays)

    private tailrec fun getWorkDayByCalendar(country: String, dateStartAuction: LocalDate): LocalDate {
        val year = dateStartAuction.year
        val month = dateStartAuction.month.value
        val days = calendarRepository.loadWorkDays(country, year, month)
        if (days.isEmpty())
            throw CalendarNoDataException("Calendar no contains data for country: $country, year: $year, month: $month")

        val numberDay = dateStartAuction.dayOfMonth
        return if (days.contains(numberDay))
            dateStartAuction
        else
            getWorkDayByCalendar(country, dateStartAuction.plusDays(1))
    }

    private fun genPlannedAuctions(startDate: LocalDate,
                                   lotsInfo: LotsInfo,
                                   booked: Map<SlotDefinition, List<LotsInfo.Detail>>): PlannedAuction {
        val result = LinkedHashMap<RelatedLot, PlannedAuction.Lot>()
        var minTime: LocalTime? = null
        for ((slotDetail, lotsDetails) in booked) {
            var startDateTimeSlot = LocalDateTime.of(startDate, slotDetail.startTime)

            if (minTime == null || minTime.isAfter(slotDetail.startTime))
                minTime = slotDetail.startTime

            for (lotDetail in lotsDetails) {
                result[lotDetail.relatedLot] = PlannedAuction.Lot(
                    id = UUID.randomUUID(),
                    startDateTime = startDateTimeSlot,
                    electronicAuctionModalities = lotDetail.electronicAuctionModalities
                        .map {
                            PlannedAuction.Lot.ElectronicAuctionModality(
                                url = genUrl(lotsInfo.cpid, lotDetail.relatedLot),
                                eligibleMinimumDifference = PlannedAuction.Lot.ElectronicAuctionModality.EligibleMinimumDifference(
                                    amount = it.eligibleMinimumDifference.amount,
                                    currency = it.eligibleMinimumDifference.currency
                                )
                            )
                        }
                )

                startDateTimeSlot = startDateTimeSlot.plus(lotDetail.duration)
            }
        }

        return PlannedAuction(
            version = GlobalProperties.Auction.apiVersion,
            startDateTime = LocalDateTime.of(startDate, minTime),
            lots = result,
            usedSlots = booked.keys.asSequence().map { it.keyOfSlot }.toSet()
        )
    }

    private fun genUrl(cpid: String, relatedLot: RelatedLot) = "https://eauction.mtender.md/$cpid/$relatedLot"

    private fun savePlannedAuctions(request: ScheduleRQ,
                                    plannedAuction: PlannedAuction): PlannedAuction {
        val operationHistory = PlannedAuctionKey(
            cpid = request.context.cpid,
            operationId = request.context.operationId,
            operationDate = request.context.operationDate
        )
        return plannedAuctionRepository.insert(operationHistory, plannedAuction)
    }
}

