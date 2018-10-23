package com.procurement.auction.service

import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.converter.AuctionConversionService
import com.procurement.auction.converter.convert
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.Country
import com.procurement.auction.domain.OperationId
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.request.schedule.ScheduleRQ
import com.procurement.auction.domain.response.schedule.ScheduleRS
import com.procurement.auction.domain.schedule.AuctionsDefinition
import com.procurement.auction.domain.schedule.SlotDefinition
import com.procurement.auction.entity.schedule.ScheduledAuctions
import com.procurement.auction.exception.CalendarNoDataException
import com.procurement.auction.exception.NoLotsToScheduleAuctionsException
import com.procurement.auction.repository.CalendarRepository
import com.procurement.auction.repository.ScheduledAuctionKey
import com.procurement.auction.repository.ScheduledAuctionsRepository
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

interface ScheduleService {
    fun schedule(request: ScheduleRQ): ScheduleRS
}

@Service
class ScheduleServiceImpl(private val auctionProperties: AuctionProperties,
                          private val scheduledAuctionsRepository: ScheduledAuctionsRepository,
                          private val calendarRepository: CalendarRepository,
                          private val slotsService: SlotsService,
                          private val auctionConversionService: AuctionConversionService
) : ScheduleService {

    companion object {
        private const val dateOffsetDays = 1L
    }

    private val urlAuction: String = genUrlAuctions()

    override fun schedule(request: ScheduleRQ): ScheduleRS {
        if (request.data.electronicAuctions.details.isEmpty())
            throw NoLotsToScheduleAuctionsException()

        val scheduledAuctions = loadScheduledAuctions(request.context.cpid, request.context.operationId)
            ?: schedulingAuctions(request)

        return genResponse(request, scheduledAuctions)
    }

    private fun loadScheduledAuctions(cpid: CPID, operationId: OperationId): ScheduledAuctions? {
        return scheduledAuctionsRepository.load(cpid, operationId)
    }

    private fun schedulingAuctions(request: ScheduleRQ): ScheduledAuctions {
        val auctionsDefinition = genAuctionsDefinition(request)
        var dateStartAuction = minDateOfStartAuction(auctionsDefinition.tenderPeriodEnd)
        val country = auctionsDefinition.country
        while (true) {
            val selectedDate = getWorkDayByCalendar(country, dateStartAuction)
            val slots = slotsService.loadSlots(selectedDate, country)
            val booked = slotsService.booking(auctionsDefinition.details, slots)
            if (booked.isNotEmpty()) {
                val newScheduledAuctions =
                    genScheduledAuctions(startDate = selectedDate,
                        auctionsDefinition = auctionsDefinition,
                        booked = booked)
                val actualScheduledAuctions = saveScheduledAuctions(request, newScheduledAuctions)
                if (newScheduledAuctions != actualScheduledAuctions) {
                    return actualScheduledAuctions
                }

                val cpid = request.context.cpid
                slotsService.saveSlots(cpid = cpid,
                    bookedSlots = booked.keys.asSequence().map { it.keyOfSlot }.toSet(),
                    slots = slots)
                return newScheduledAuctions
            }

            dateStartAuction = dateStartAuction.plusDays(1)
        }
    }

    private fun genAuctionsDefinition(request: ScheduleRQ): AuctionsDefinition {
        val auctionsDefinition: AuctionsDefinition = auctionConversionService.convert(request)
        slotsService.validateCountLots(auctionsDefinition)
        return auctionsDefinition
    }

    private fun minDateOfStartAuction(endDate: LocalDate): LocalDate = endDate.plusDays(dateOffsetDays)

    private tailrec fun getWorkDayByCalendar(country: Country, dateStartAuction: LocalDate): LocalDate {
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

    private fun genScheduledAuctions(startDate: LocalDate,
                                     auctionsDefinition: AuctionsDefinition,
                                     booked: Map<SlotDefinition, List<AuctionsDefinition.Detail>>): ScheduledAuctions {
        val result = mutableListOf<ScheduledAuctions.ElectronicAuctions.Detail>()
        var minTime: LocalTime? = null
        for ((slotDefinition, auctionsDetails) in booked) {
            var startDateTimeSlot = LocalDateTime.of(startDate, slotDefinition.startTime)

            if (minTime == null || minTime.isAfter(slotDefinition.startTime))
                minTime = slotDefinition.startTime

            for (auctionDetail in auctionsDetails) {
                result.add(
                    ScheduledAuctions.ElectronicAuctions.Detail(
                        id = UUID.randomUUID(),
                        relatedLot = auctionDetail.relatedLot,
                        auctionPeriod = ScheduledAuctions.ElectronicAuctions.Detail.AuctionPeriod(
                            startDateTime = startDateTimeSlot
                        ),
                        electronicAuctionModalities = auctionDetail.electronicAuctionModalities
                            .map {
                                ScheduledAuctions.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                    url = genUrl(auctionsDefinition.cpid, auctionDetail.relatedLot),
                                    eligibleMinimumDifference = ScheduledAuctions.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                        amount = it.eligibleMinimumDifference.amount,
                                        currency = it.eligibleMinimumDifference.currency
                                    )
                                )
                            }
                    )
                )

                startDateTimeSlot = startDateTimeSlot.plus(auctionDetail.duration)
            }
        }

        return ScheduledAuctions(
            version = GlobalProperties.AuctionSchedule.apiVersion,
            usedSlots = booked.keys.asSequence().map { it.keyOfSlot }.toSet(),
            auctionPeriod = ScheduledAuctions.AuctionPeriod(
                startDateTime = LocalDateTime.of(startDate, minTime)
            ),
            electronicAuctions = ScheduledAuctions.ElectronicAuctions(
                details = result
            )
        )
    }

    private fun genUrl(cpid: CPID, relatedLot: RelatedLot): String = "$urlAuction/auctions/$cpid/$relatedLot"

    private fun saveScheduledAuctions(request: ScheduleRQ,
                                      scheduledAuctions: ScheduledAuctions): ScheduledAuctions {
        val operationHistory = ScheduledAuctionKey(
            cpid = request.context.cpid,
            operationId = request.context.operationId,
            operationDate = request.context.operationDate
        )
        return scheduledAuctionsRepository.insert(operationHistory, scheduledAuctions)
    }

    private fun genUrlAuctions(): String {
        val url = auctionProperties.url
            ?: throw IllegalStateException("Not set the url of an auction.")
        val protocol = url.protocol
            ?: throw IllegalStateException("Not set the scheme of the url.")
        val host = url.host
            ?: throw IllegalStateException("Not set the domain name of the url.")
        return getUrlASCII("$protocol://$host")
    }

    private fun getUrlASCII(uri: String) = try {
        URL(uri).toURI().toASCIIString()
    } catch (exception: Exception) {
        throw IllegalStateException("Invalid the auction url: '$uri'.")
    }

    private fun genResponse(request: ScheduleRQ, scheduledAuctions: ScheduledAuctions): ScheduleRS {
        return ScheduleRS(
            id = request.id,
            version = GlobalProperties.App.apiVersion,
            data = ScheduleRS.Data(
                auctionPeriod = ScheduleRS.Data.AuctionPeriod(
                    startDate = scheduledAuctions.auctionPeriod.startDateTime
                ),
                electronicAuctions = ScheduleRS.Data.ElectronicAuctions(
                    details = scheduledAuctions.electronicAuctions.details
                        .map { detail ->
                            ScheduleRS.Data.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = detail.relatedLot,
                                auctionPeriod = ScheduleRS.Data.ElectronicAuctions.Detail.AuctionPeriod(
                                    startDate = detail.auctionPeriod.startDateTime
                                ),
                                electronicAuctionModalities = detail.electronicAuctionModalities
                                    .map { electronicAuctionModality ->
                                        ScheduleRS.Data.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                            url = electronicAuctionModality.url,
                                            eligibleMinimumDifference = ScheduleRS.Data.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                                amount = electronicAuctionModality.eligibleMinimumDifference.amount,
                                                currency = electronicAuctionModality.eligibleMinimumDifference.currency
                                            )
                                        )
                                    }
                            )
                        }
                )
            )
        )
    }
}

