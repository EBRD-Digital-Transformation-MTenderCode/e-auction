package com.procurement.auction.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.domain.request.Command
import com.procurement.auction.domain.request.CommandRQ
import com.procurement.auction.domain.request.auction.EndRQ
import com.procurement.auction.domain.request.auction.StartRQ
import com.procurement.auction.domain.request.schedule.ScheduleRQ
import com.procurement.auction.service.AuctionEndService
import com.procurement.auction.service.AuctionStartService
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
                         private val scheduleService: ScheduleService,
                         private val auctionStartService: AuctionStartService,
                         private val auctionEndService: AuctionEndService
) {

    @PostMapping()
    fun schedule(request: HttpServletRequest): ResponseEntity<*> {
        val body = request.reader.readText()
        val command = objectMapper.toObject<CommandRQ>(body)

        when (command.command) {
            Command.SCHEDULE -> {
                val data = objectMapper.toObject<ScheduleRQ>(body)
                val response = scheduleService.schedule(data)
                return ResponseEntity.ok(response)
            }
            Command.AUCTIONS_START -> {
                val data = objectMapper.toObject<StartRQ>(body)
                val response = auctionStartService.start(data)
                return ResponseEntity.ok(response)
            }
            Command.AUCTIONS_END -> {
                val data = objectMapper.toObject<EndRQ>(body)
                val response = auctionEndService.end(data)
                return ResponseEntity.ok(response)
            }
        }
    }
}