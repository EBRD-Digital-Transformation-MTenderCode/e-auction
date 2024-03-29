package com.procurement.auction.application.service.auctions

import com.procurement.auction.application.auctionDuration
import com.procurement.auction.application.countAuctions
import com.procurement.auction.application.params.auction.validate.ValidateAuctionsDataParams
import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import com.procurement.auction.domain.extension.asSet
import com.procurement.auction.domain.extension.getDuplicate
import com.procurement.auction.domain.extension.isNotUnique
import com.procurement.auction.domain.extension.toSetBy
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.fail.error.ValidationError
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.ValidationResult
import com.procurement.auction.domain.functional.asFailure
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.domain.functional.bind
import com.procurement.auction.domain.model.date.JsonTimeDeserializer
import com.procurement.auction.domain.model.enums.OperationType
import com.procurement.auction.domain.model.lotId.TemporalLotId
import com.procurement.auction.exception.app.DuplicateLotException
import com.procurement.auction.exception.app.InvalidElectronicAuctionsException
import com.procurement.auction.exception.app.InvalidLotsException
import com.procurement.auction.exception.app.NoLotsException
import com.procurement.auction.infrastructure.dto.command.ValidateAuctionsCommand
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalTime

interface ValidateAuctionsService {
    fun validate(command: ValidateAuctionsCommand): ValidatedAuctions
    fun validateAuctionsData(params: ValidateAuctionsDataParams): ValidationResult<Fail>
}

@Service
class ValidateAuctionsServiceImpl(
    auctionProperties: AuctionProperties,
    schedulerProperties: SchedulerProperties
) : ValidateAuctionsService {

    companion object {
        private val TEN_PERCENT = BigDecimal(0.1)
    }

    private val maxCountAuctions: Long

    init {
        val auctionDuration = auctionDuration(
            durationOneStep = auctionProperties.durationOneStep!!,
            durationPauseAfterStep = auctionProperties.durationPauseAfterStep!!,
            qtyParticipants = auctionProperties.qtyParticipants!!.toLong(),
            qtyRounds = auctionProperties.qtyRounds!!,
            durationPauseAfterAuction = auctionProperties.durationPauseAfterAuction!!
        )

        val slots: List<Pair<LocalTime, LocalTime>> = schedulerProperties.slots!!
            .map { slot ->
                val start = JsonTimeDeserializer.deserialize(slot.startTime!!)
                val end = JsonTimeDeserializer.deserialize(slot.endTime ?: schedulerProperties.endTimeAllSlots!!)
                start to end
            }

        maxCountAuctions = countAuctions(auctionDuration, slots)
    }

    override fun validate(command: ValidateAuctionsCommand): ValidatedAuctions {
        checkAuctionLots(auctionLots = command.data.lots)
        checkElectronicAuctionsDetails(electronicAuctionsDetails = command.data.electronicAuctions.details)
        checkRelatedLotInElectronicAuctions(data = command.data)
        checkLots(data = command.data)
        checkValueInElectronicAuctions(data = command.data)
        checkMaximumNumberElectronicAuctions(electronicAuctions = command.data.electronicAuctions.details)
        return ValidatedAuctions()
    }

    /**
     * FReq-1.1.1.16
     */
    private fun checkAuctionLots(auctionLots: List<ValidateAuctionsCommand.Data.Lot>) {
        if (auctionLots.isEmpty())
            throw NoLotsException()

        if (auctionLots.isNotUnique { it.id })
            throw DuplicateLotException()
    }

    private fun checkElectronicAuctionsDetails(electronicAuctionsDetails: List<ValidateAuctionsCommand.Data.ElectronicAuctions.Detail>) {
        if (electronicAuctionsDetails.isEmpty())
            throw InvalidElectronicAuctionsException(message = "Electronic auctions are empty.")

        if (electronicAuctionsDetails.isNotUnique { it.id })
            throw InvalidElectronicAuctionsException(message = "Electronic auctions contain duplicate identifiers.")

        electronicAuctionsDetails.forEach { auctionDetails ->
            checkElectronicAuctionsModalities(auctionDetails.electronicAuctionModalities)
        }
    }

    private fun checkElectronicAuctionsModalities(
        electronicAuctionsModalities: List<ValidateAuctionsCommand.Data.ElectronicAuctions.Detail.ElectronicAuctionModalities>
    ) {
        if (electronicAuctionsModalities.isEmpty())
            throw InvalidElectronicAuctionsException(message = "Electronic auctions modalities are empty.")
    }

    /**
     * FReq-1.1.1.18
     */
    private fun checkRelatedLotInElectronicAuctions(data: ValidateAuctionsCommand.Data) {
        val lotsIds: Set<TemporalLotId> = data.lots.toSetBy { it.id }

        data.electronicAuctions.details.forEach { detail ->
            if (detail.relatedLot !in lotsIds)
                throw InvalidElectronicAuctionsException(message = "Electronic auctions contain an invalid related lot: '${detail.relatedLot}'.")
        }
    }

    /**
     * FReq-1.1.1.19
     */
    private fun checkLots(data: ValidateAuctionsCommand.Data) {
        val electronicAuctionsByRelatedLot: Map<TemporalLotId, ValidateAuctionsCommand.Data.ElectronicAuctions.Detail> =
            data.electronicAuctions.details.associateBy { it.relatedLot }

        data.lots.forEach { lot ->
            if (lot.id !in electronicAuctionsByRelatedLot)
                throw InvalidLotsException("Lot with id: ${lot.id} not relate to some the electronic auction.")
        }

        val duplicateReferenceToLot = data.electronicAuctions.details.getDuplicate { it.relatedLot }
        if (duplicateReferenceToLot != null)
            throw InvalidLotsException("Lot with id: $duplicateReferenceToLot references more than one electronic auctions' details.")
    }

    /**
     * FReq-1.1.1.20
     */
    private fun checkValueInElectronicAuctions(data: ValidateAuctionsCommand.Data) {
        val lotsById: Map<TemporalLotId, ValidateAuctionsCommand.Data.Lot> =
            data.lots.associateBy { it.id }

        data.electronicAuctions.details.forEach { auction ->
            auction.electronicAuctionModalities.forEach { modality ->
                val eligibleMinimumDifference = modality.eligibleMinimumDifference
                val lot: ValidateAuctionsCommand.Data.Lot = lotsById.getValue(auction.relatedLot)


                if (eligibleMinimumDifference.currency != lot.value.currency)
                    throw InvalidElectronicAuctionsException("Electronic auction with id: '${auction.id}' contain invalid currency in 'EligibleMinimumDifference' attribute.")

                val bid: BigDecimal = lot.value.amount.value.times(TEN_PERCENT)
                if (eligibleMinimumDifference.amount.value > bid)
                    throw InvalidElectronicAuctionsException("Electronic auction with id: '${auction.id}' contain invalid amount in 'EligibleMinimumDifference' attribute. (amount value is more than the allowable auction step)")
            }
        }
    }

    /**
     * FReq-1.1.1.21
     */
    private fun checkMaximumNumberElectronicAuctions(electronicAuctions: List<ValidateAuctionsCommand.Data.ElectronicAuctions.Detail>) {
        if (electronicAuctions.size > maxCountAuctions)
            throw InvalidElectronicAuctionsException(message = "The number of electronic auctions more than the allowable number.")
    }

    override fun validateAuctionsData(params: ValidateAuctionsDataParams): ValidationResult<Fail> {
        checkForAuctionDuplicates(params)  // VR.COM-18.1.1
            .bind { checkValuePresent(it) } // VR.COM-18.1.6, VR.COM-18.1.7, VR.COM-18.1.8
            .bind { checkForMissingLots(it) } // VR.COM-18.1.2
            .bind { checkEachLotIsLinkedToOneAuction(it) } // VR.COM-18.1.3
            .bind { checkForUnmatchingCurrency(it) } // VR.COM-18.1.4,  VR.COM-18.1.5
            .doOnError { error -> return ValidationResult.error(error) }

        return ValidationResult.ok()
    }

    private fun checkValuePresent(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> =
        when(params.operationType) {
            OperationType.CREATE_PCR -> checkTenderCurrencyPresent(params)
            OperationType.CREATE_RFQ -> {
                checkLotsValuePresent(params)
                checkAmountPresent(params)
            }
        }

    private fun checkTenderCurrencyPresent(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> =
        if (params.tender.value == null)
            ValidationError.MissingTenderValue().asFailure()
        else
            params.asSuccess()

    private fun checkLotsValuePresent(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> {
        val lotsWithoutValue = params.tender.lots.filter { it.value == null }

        if (lotsWithoutValue.isNotEmpty())
            return ValidationError.MissingLotValue(lotsWithoutValue.toSetBy { it.id }).asFailure()
        else
            return params.asSuccess()
    }

    private fun checkAmountPresent(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> {
        params.tender.electronicAuctions.details
            .forEach { auction ->
                if (auction.isMissingAmount())
                    return ValidationError.MissingAmountValue(auction.id).asFailure()
            }

        return params.asSuccess()
    }

    private fun ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.isMissingAmount() =
        electronicAuctionModalities.any { it.eligibleMinimumDifference.amount == null }

    private fun checkForAuctionDuplicates(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> {
        val duplicateAuction = params.tender.electronicAuctions.details.getDuplicate { it.id }
        if (duplicateAuction != null)
            return ValidationError.DuplicateElectronicAuctionIds(duplicateAuction.id).asFailure()

        return params.asSuccess()
    }

    private fun checkForUnmatchingCurrency(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> =
        when (params.operationType) {
            OperationType.CREATE_PCR -> checkForUnmatchingCurrencyInTender(params)
            OperationType.CREATE_RFQ -> checkForUnmatchingCurrencyInLots(params)
        }

    private fun checkForUnmatchingCurrencyInTender(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> {
        val tenderCurrency = params.tender.value!!.currency

        val auctionWithModalitiesContainingInvalidCurrency = params.tender.electronicAuctions.details
            .filter { auction ->
                auction.electronicAuctionModalities
                    .any { it.eligibleMinimumDifference.currency != tenderCurrency }
            }

        if (auctionWithModalitiesContainingInvalidCurrency.isNotEmpty())
            return ValidationError.AuctionModalityMismatchWithTenderCurrency(
                auctionIds = auctionWithModalitiesContainingInvalidCurrency.map { it.id },
                tenderCurrency = tenderCurrency
            ).asFailure()

        return params.asSuccess()
    }

    private fun checkForUnmatchingCurrencyInLots(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> {
        val lotCurrencies = params.tender.lots.map { it.value!!.currency }

        val auctionWithModalitiesContainingInvalidCurrency = params.tender.electronicAuctions.details
            .filter { auction ->
                auction.electronicAuctionModalities
                    .any { it.eligibleMinimumDifference.currency !in lotCurrencies }
            }

        if (auctionWithModalitiesContainingInvalidCurrency.isNotEmpty())
            return ValidationError.AuctionModalityMismatchWithLotsCurrency(
                auctionIds = auctionWithModalitiesContainingInvalidCurrency.map { it.id },
                lotsCurrencies = lotCurrencies
            ).asFailure()

        return params.asSuccess()
    }

    private fun checkForMissingLots(params: ValidateAuctionsDataParams): Result<ValidateAuctionsDataParams, Fail> {
        val relatedLots = params.tender.electronicAuctions.details.map { it.relatedLot }
        val lots = params.tender.lots.map { it.id }
        val missingLots = relatedLots.asSet().subtract(lots.asSet())
        if (missingLots.isNotEmpty())
            return ValidationError.LinkedLotNotFound(missingLots).asFailure()

        return params.asSuccess()
    }

    private fun checkEachLotIsLinkedToOneAuction(
        params: ValidateAuctionsDataParams
    ): Result<ValidateAuctionsDataParams, Fail> {
        val lots = params.tender.lots.map { it.id }
        val auctionsNumberByLots = params.tender.electronicAuctions.details.groupingBy { it.relatedLot }.eachCount()
        lots.forEach() {
            if (auctionsNumberByLots[it] != 1)
                return ValidationError.LotMustBeLinkedToOneAuction(it).asFailure()
        }

        return params.asSuccess()
    }
}
