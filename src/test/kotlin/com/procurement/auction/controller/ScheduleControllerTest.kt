package com.procurement.auction.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.auction.AbstractBase
import com.procurement.auction.domain.Amount
import com.procurement.auction.domain.ApiVersion
import com.procurement.auction.domain.AuctionId
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.CommandId
import com.procurement.auction.domain.Currency
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.response.schedule.ScheduleRS
import com.procurement.auction.exception.JsonParseToObjectException
import com.procurement.auction.service.AuctionEndService
import com.procurement.auction.service.AuctionStartService
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

        private val commandId: CommandId = UUID.fromString("96977fc8-9ef1-444c-9e3c-90b1db361173")
        private const val cpid: CPID = "cpid-1"
        private val auctionId: AuctionId = UUID.fromString("b405fe10-f954-400e-bc39-49a03948991a")
        private val relatedLot: RelatedLot = UUID.fromString("358404aa-93b9-41b0-be7d-ab5f8db01123")
        private const val auctionsStartDateText = "2018-09-14T08:48:17Z"
        private val auctionsStartDate = JsonDateTimeDeserializer.deserialize(auctionsStartDateText)
        private const val lotStartDateText = "2018-09-15T08:48:17Z"
        private val lotStartDate = JsonDateTimeDeserializer.deserialize(lotStartDateText)
        private val auctionUrl = "https://eauction.mtender.md/$cpid/$relatedLot"
        private const val amount: Amount = 150.0
        private const val currency: Currency = "MDL"
        private val apiVersion = ApiVersion(1, 0, 0)

        private val scheduleRS = ScheduleRS(
            id = commandId,
            version = apiVersion,
            data = ScheduleRS.Data(
                auctionPeriod = ScheduleRS.Data.AuctionPeriod(
                    startDate = auctionsStartDate
                ),
                electronicAuctions = ScheduleRS.Data.ElectronicAuctions(
                    details = listOf(
                        ScheduleRS.Data.ElectronicAuctions.Detail(
                            id = auctionId,
                            relatedLot = relatedLot,
                            auctionPeriod = ScheduleRS.Data.ElectronicAuctions.Detail.AuctionPeriod(
                                startDate = lotStartDate
                            ),
                            electronicAuctionModalities = listOf(
                                ScheduleRS.Data.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                    url = auctionUrl,
                                    eligibleMinimumDifference = ScheduleRS.Data.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                        amount = amount,
                                        currency = currency
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    private val objectMapper = ObjectMapper()
    private lateinit var scheduleService: ScheduleService
    private lateinit var auctionStartService: AuctionStartService
    private lateinit var auctionEndService: AuctionEndService
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        scheduleService = mock()
        auctionStartService = mock()
        auctionEndService = mock()
        val controller = ScheduleController(objectMapper, scheduleService, auctionStartService, auctionEndService)
        val exceptionHandler = WebExceptionHandler()
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(exceptionHandler)
            .build()
    }

    @Test
    @DisplayName("Success")
    fun schedule() {
        val content = RESOURCES.load("json/schedule/request.json")

        whenever(scheduleService.schedule(any()))
            .thenReturn(scheduleRS)

        mockMvc.perform(
            post(URL)
                .contentType("application/json;charset=UTF-8")
                .content(content))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo("$commandId")))
            .andExpect(jsonPath("$.data.auctionPeriod.startDate", equalTo(auctionsStartDateText)))
            .andExpect(jsonPath("$.data.electronicAuctions.details.length()", equalTo(1)))
            .andExpect(jsonPath("$.data.electronicAuctions.details[0].id", equalTo("$auctionId")))
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
    fun scheduleBedRequest() {
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
