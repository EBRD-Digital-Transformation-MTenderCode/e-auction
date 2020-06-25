package com.procurement.auction.application.service.auctions

import com.procurement.auction.application.auctionDuration
import com.procurement.auction.application.countAuctions
import com.procurement.auction.application.getDuplicate
import com.procurement.auction.application.isNotUnique
import com.procurement.auction.application.toSetBy
import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import com.procurement.auction.domain.model.date.JsonTimeDeserializer
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
}
