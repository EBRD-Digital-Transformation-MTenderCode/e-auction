package com.procurement.auction.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.domain.request.Command
import com.procurement.auction.domain.request.schedule.ScheduleRQ
import com.procurement.auction.domain.response.schedule.ScheduleRS
import com.procurement.auction.domain.schedule.PlannedAuction
import com.procurement.auction.service.ScheduleService
import com.procurement.auction.service.toObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/command")
class ScheduleController(private val objectMapper: ObjectMapper,
                         private val scheduleService: ScheduleService) {

    @PostMapping()
    fun schedule(request: HttpServletRequest): ResponseEntity<ScheduleRS> {
        val body = request.reader.readText()
        val data = objectMapper.toObject<ScheduleRQ>(body)

        when (data.command) {
            Command.SCHEDULE -> {
                val auctionPlanningInfo = scheduleService.schedule(data)
                val response = genResponse(request = data, plannedAuction = auctionPlanningInfo)
                return ResponseEntity.ok(response)
            }
        }
    }

    private fun genResponse(request: ScheduleRQ, plannedAuction: PlannedAuction): ScheduleRS {
        return ScheduleRS(
            id = request.id,
            version = GlobalProperties.App.apiVersion,
            data = ScheduleRS.Data(
                auctionPeriod = ScheduleRS.Data.AuctionPeriod(
                    startDate = plannedAuction.startDateTime
                ),
                electronicAuctions = ScheduleRS.Data.ElectronicAuctions(
                    details = plannedAuction.lots.map {
                        val relatedLot = it.key
                        val lot = it.value

                        ScheduleRS.Data.ElectronicAuctions.Detail(
                            id = lot.id,
                            relatedLot = relatedLot,
                            auctionPeriod = ScheduleRS.Data.ElectronicAuctions.Detail.AuctionPeriod(
                                startDate = lot.startDateTime
                            ),
                            electronicAuctionModalities = lot.electronicAuctionModalities
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