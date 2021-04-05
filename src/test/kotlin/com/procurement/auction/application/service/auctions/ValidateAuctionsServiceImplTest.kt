package com.procurement.auction.application.service.auctions

import com.procurement.auction.application.params.auction.validate.ValidateAuctionsDataParams
import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import com.procurement.auction.domain.functional.ValidationResult
import com.procurement.auction.domain.model.enums.OperationType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

internal class ValidateAuctionsServiceImplTest {
    companion object {
        private val CURRENCY = "currency"
        private val LOT_ID_FIRST = UUID.randomUUID().toString()
        private val LOT_ID_SECOND = UUID.randomUUID().toString()
        private val ELECTRONIC_AUCTIONS_ID_FIRST = UUID.randomUUID().toString()
        private val ELECTRONIC_AUCTIONS_ID_SECOND = UUID.randomUUID().toString()
    }

    private lateinit var validateAuctionsService: ValidateAuctionsService

    @BeforeEach
    fun init() {
        validateAuctionsService = ValidateAuctionsServiceImpl(AuctionProperties(), SchedulerProperties())
    }

    @Nested
    inner class ValidateAuctionsData {
        @Test
        fun validateAuctionsData_success() {
            val auctionsWithDuplicateIds = listOf(
                Auctions(id = ELECTRONIC_AUCTIONS_ID_FIRST, relatedLot = LOT_ID_FIRST, currency = CURRENCY),
                Auctions(id = ELECTRONIC_AUCTIONS_ID_SECOND, relatedLot = LOT_ID_SECOND, currency = CURRENCY)
            )
            val validParams = getParamsForCreatePcr(
                lotIds = listOf(LOT_ID_FIRST, LOT_ID_SECOND),
                valueCurrency = CURRENCY,
                auctions = auctionsWithDuplicateIds
            )
            val result = validateAuctionsService.validateAuctionsData(validParams)
            assertTrue(result is ValidationResult.Ok)
        }

        @Test
        fun unUniqueAuctionIdReceived_fail() {
            val unUniqueAuctionId = ELECTRONIC_AUCTIONS_ID_FIRST

            val auctionsWithDuplicateIds = listOf(
                Auctions(id = unUniqueAuctionId, relatedLot = LOT_ID_FIRST, currency = CURRENCY),
                Auctions(id = unUniqueAuctionId, relatedLot = LOT_ID_SECOND, currency = CURRENCY)
            )
            val params = getParamsForCreatePcr(
                lotIds = listOf(LOT_ID_FIRST, LOT_ID_SECOND),
                valueCurrency = CURRENCY,
                auctions = auctionsWithDuplicateIds
            )

            val error = validateAuctionsService.validateAuctionsData(params).error

            val expectedErrorCode = "VR.COM-18.1.1"
            val expectedDescription = "Found duplicate electronicAuctions.details.id '$unUniqueAuctionId'"

            assertEquals(expectedErrorCode, error.code)
            assertEquals(expectedDescription, error.description)
        }

        @Test
        fun linkedToAuctionLotNotReceived_fail() {
            val lotIdReceived = LOT_ID_FIRST
            val lotIdMissing = LOT_ID_SECOND

            val auctionsWithDuplicateIds = listOf(
                Auctions(id = ELECTRONIC_AUCTIONS_ID_FIRST, relatedLot = lotIdReceived, currency = CURRENCY),
                Auctions(id = ELECTRONIC_AUCTIONS_ID_SECOND, relatedLot = lotIdMissing, currency = CURRENCY)
            )
            val params = getParamsForCreatePcr(
                lotIds = listOf(lotIdReceived),
                valueCurrency = CURRENCY,
                auctions = auctionsWithDuplicateIds
            )

            val error = validateAuctionsService.validateAuctionsData(params).error

            val expectedErrorCode = "VR.COM-18.1.2"
            val expectedDescription = "Missing lots: '$lotIdMissing'"

            assertEquals(expectedErrorCode, error.code)
            assertEquals(expectedDescription, error.description)
        }

        @Test
        fun lotMustBeLinkedToOneAuction_fail() {
            val lotLinkedToTwoAuctions = LOT_ID_FIRST
            val auctionsWithDuplicateIds = listOf(
                Auctions(id = ELECTRONIC_AUCTIONS_ID_FIRST, relatedLot = lotLinkedToTwoAuctions, currency = CURRENCY),
                Auctions(id = ELECTRONIC_AUCTIONS_ID_SECOND, relatedLot = lotLinkedToTwoAuctions, currency = CURRENCY)
            )
            val params = getParamsForCreatePcr(
                lotIds = listOf(lotLinkedToTwoAuctions),
                valueCurrency = CURRENCY,
                auctions = auctionsWithDuplicateIds
            )

            val error = validateAuctionsService.validateAuctionsData(params).error

            val expectedErrorCode = "VR.COM-18.1.3"
            val expectedDescription = "Lot '$lotLinkedToTwoAuctions' must be linked to one and only one electronic auction"

            assertEquals(expectedErrorCode, error.code)
            assertEquals(expectedDescription, error.description)
        }

        @Test
        fun lotNotLinkedToAuction_fail() {
            val linkedToAuctionLot = LOT_ID_FIRST
            val notLinkedToAuctionLot = LOT_ID_SECOND

            val auctionsWithDuplicateIds = listOf(
                Auctions(id = ELECTRONIC_AUCTIONS_ID_FIRST, relatedLot = linkedToAuctionLot, currency = CURRENCY)
            )
            val params = getParamsForCreatePcr(
                lotIds = listOf(linkedToAuctionLot, notLinkedToAuctionLot),
                valueCurrency = CURRENCY,
                auctions = auctionsWithDuplicateIds
            )

            val error = validateAuctionsService.validateAuctionsData(params).error

            val expectedErrorCode = "VR.COM-18.1.3"
            val expectedDescription = "Lot '$notLinkedToAuctionLot' must be linked to one and only one electronic auction"

            assertEquals(expectedErrorCode, error.code)
            assertEquals(expectedDescription, error.description)
        }

        @Test
        fun currencyDoesNotMatch_fail() {
            val unMatchingCurrency = "unMatchingCurrency"

            val auctionsWithDuplicateIds = listOf(
                Auctions(id = ELECTRONIC_AUCTIONS_ID_FIRST, relatedLot = LOT_ID_FIRST, currency = CURRENCY),
                Auctions(id = ELECTRONIC_AUCTIONS_ID_SECOND, relatedLot = LOT_ID_SECOND, currency = unMatchingCurrency)
            )
            val params = getParamsForCreatePcr(
                lotIds = listOf(LOT_ID_FIRST, LOT_ID_SECOND),
                valueCurrency = CURRENCY,
                auctions = auctionsWithDuplicateIds
            )

            val error = validateAuctionsService.validateAuctionsData(params).error

            val expectedErrorCode = "VR.COM-18.1.4"
            val expectedDescription = "Electronic auctions '${ELECTRONIC_AUCTIONS_ID_SECOND}' contain modality(s) with currency that does not match tender currency '$CURRENCY'"

            assertEquals(expectedErrorCode, error.code)
            assertEquals(expectedDescription, error.description)
        }

        inner class Auctions(val id: String, val relatedLot: String, val currency: String)

        private fun getParamsForCreatePcr(
            lotIds: List<String>,
            valueCurrency: String,
            auctions: List<Auctions>
        ) = ValidateAuctionsDataParams.tryCreate(
            operationType = OperationType.CREATE_PCR.toString(),
            tender = ValidateAuctionsDataParams.Tender.tryCreate(
                value = ValidateAuctionsDataParams.Tender.Value(currency = valueCurrency),
                lots = lotIds.map { ValidateAuctionsDataParams.Tender.Lot(it, null) },
                electronicAuctions = ValidateAuctionsDataParams.Tender.ElectronicAuctions.tryCreate(
                    auctions.map {
                        ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.tryCreate(
                            id = it.id,
                            relatedLot = it.relatedLot,
                            electronicAuctionModalities = listOf(
                                ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                    ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                        it.currency
                                    )
                                )
                            )
                        ).get
                    }
                ).get
            ).get
        ).get
    }
}