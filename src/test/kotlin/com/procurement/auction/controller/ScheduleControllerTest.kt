package com.procurement.auction.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.auction.AbstractBase
import com.procurement.auction.domain.ApiVersion
import com.procurement.auction.domain.LotId
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.schedule.PlannedAuction
import com.procurement.auction.exception.JsonParseToObjectException
import com.procurement.auction.service.ScheduleService
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

class ScheduleControllerTest : AbstractBase() {
    companion object {
        private const val URL = "/command"
        private const val INVALID_PAYLOAD = "{}"

        private val requestId: UUID = UUID.fromString("96977fc8-9ef1-444c-9e3c-90b1db361173")
        private const val cpid = "cpid-1"
        private val lotid: UUID = UUID.fromString("b405fe10-f954-400e-bc39-49a03948991a")
        private val relatedLot: UUID = UUID.fromString("358404aa-93b9-41b0-be7d-ab5f8db01123")
        private const val auctionsStartDateText = "2018-09-14T08:48:17Z"
        private val auctionsStartDate = JsonDateTimeDeserializer.deserialize(auctionsStartDateText)
        private const val lotStartDateText = "2018-09-15T08:48:17Z"
        private val lotStartDate = JsonDateTimeDeserializer.deserialize(lotStartDateText)
        private val auctionUrl = "https://eauction.mtender.md/$cpid/$relatedLot"
        private const val amount = 150.0
        private const val currency = "MDL"
        private val apiVersion = ApiVersion(1, 0, 0)

        private val auctionPlanningInfo = PlannedAuction(
            version = apiVersion,
            startDateTime = auctionsStartDate,
            lots = LinkedHashMap<LotId, PlannedAuction.Lot>().apply {
                this[relatedLot] = PlannedAuction.Lot(
                    id = lotid,
                    startDateTime = lotStartDate,
                    electronicAuctionModalities = listOf(
                        PlannedAuction.Lot.ElectronicAuctionModality(
                            url = auctionUrl,
                            eligibleMinimumDifference = PlannedAuction.Lot.ElectronicAuctionModality.EligibleMinimumDifference(
                                amount = amount,
                                currency = currency
                            )
                        )
                    )
                )
            },
            usedSlots = setOf(1)
        )
    }

    private val objectMapper = ObjectMapper()
    private lateinit var scheduleService: ScheduleService
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        scheduleService = mock()
        val controller = ScheduleController(objectMapper, scheduleService)
        val exceptionHandler = WebExceptionHandler()
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(exceptionHandler)
            .build()
    }

    @Test
    @DisplayName("Success")
    fun success() {
        val content = RESOURCES.load("json/schedule/request.json")

        whenever(scheduleService.schedule(any()))
            .thenReturn(auctionPlanningInfo)

        mockMvc.perform(
            post(URL)
                .contentType("application/json;charset=UTF-8")
                .content(content))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo("$requestId")))
            .andExpect(jsonPath("$.data.auctionPeriod.startDate", equalTo(auctionsStartDateText)))
            .andExpect(jsonPath("$.data.electronicAuctions.details.length()", equalTo(1)))
            .andExpect(jsonPath("$.data.electronicAuctions.details[0].id", equalTo("$lotid")))
            .andExpect(jsonPath("$.data.electronicAuctions.details[0].relatedLot", equalTo("$relatedLot")))
            .andExpect(
                jsonPath(
                    "$.data.electronicAuctions.details[0].auctionPeriod.startDate",
                    equalTo(lotStartDateText)
                )
            )
            .andExpect(jsonPath("$.data.electronicAuctions.details[0].electronicAuctionModalities.length()",
                equalTo(1)))
            .andExpect(
                jsonPath(
                    "$.data.electronicAuctions.details[0].electronicAuctionModalities[0].url",
                    equalTo(auctionUrl)
                )
            )
            .andExpect(
                jsonPath(
                    "$.data.electronicAuctions.details[0].electronicAuctionModalities[0].eligibleMinimumDifference.amount",
                    equalTo(amount)
                )
            )
            .andExpect(
                jsonPath(
                    "$.data.electronicAuctions.details[0].electronicAuctionModalities[0].eligibleMinimumDifference.currency",
                    equalTo(currency)
                )
            )
            .andExpect(jsonPath("$.version", equalTo(apiVersion.toString())))
    }

    @Test
    @DisplayName("Invalid a payload of a request")
    fun bedRequest() {
        whenever(scheduleService.schedule(any()))
            .thenThrow(JsonParseToObjectException(INVALID_PAYLOAD, RuntimeException()))

        mockMvc.perform(
            post(URL)
                .content(INVALID_PAYLOAD))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.errors.length()", equalTo(1)))
            .andExpect(jsonPath("$.errors[0].code", equalTo("400.15.01.01")))
            .andExpect(jsonPath("$.errors[0].description", equalTo("The bad payload of request.")))
    }
}
