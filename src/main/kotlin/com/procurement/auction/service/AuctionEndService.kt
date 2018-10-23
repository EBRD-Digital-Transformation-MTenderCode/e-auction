package com.procurement.auction.service

import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.converter.AuctionConversionService
import com.procurement.auction.converter.convert
import com.procurement.auction.domain.request.auction.EndRQ
import com.procurement.auction.domain.response.auction.EndRS
import com.procurement.auction.entity.auction.EndedAuctions
import com.procurement.auction.repository.EndedAuctionsRepository
import org.springframework.stereotype.Service

interface AuctionEndService {
    fun end(endRQ: EndRQ): EndRS
}

@Service
class AuctionEndServiceImpl(
    private val endedAuctionsRepository: EndedAuctionsRepository,
    private val auctionConversionService: AuctionConversionService
) : AuctionEndService {

    override fun end(endRQ: EndRQ): EndRS {
        val endedAuctions = genEndedAuction(endRQ)

        return EndRS(
            id = endRQ.id,
            version = GlobalProperties.AuctionEnd.apiVersion,
            data = EndRS.Data(
                tender = tender(endedAuctions)
            )
        )
    }

    private fun genEndedAuction(endRQ: EndRQ): EndedAuctions {
        val cpid = endRQ.context.cpid
        val operationId = endRQ.context.operationId

        val previousEndedAuctions = endedAuctionsRepository.load(cpid = cpid)
        if (previousEndedAuctions != null) return previousEndedAuctions

        val endedAuctions: EndedAuctions = auctionConversionService.convert<EndRQ, EndedAuctions>(endRQ)

        return endedAuctionsRepository.save(cpid = cpid, operationId = operationId, endedAuctions = endedAuctions)
    }

    private fun tender(endedAuctions: EndedAuctions): EndRS.Data.Tender =
        EndRS.Data.Tender(
            auctionPeriod = EndRS.Data.Tender.AuctionPeriod(
                startDate = endedAuctions.tender.auctionPeriod.startDate,
                endDate = endedAuctions.tender.auctionPeriod.endDate
            ),
            electronicAuctions = electronicAuctions(endedAuctions)
        )

    private fun electronicAuctions(endedAuctions: EndedAuctions): EndRS.Data.Tender.ElectronicAuctions =
        EndRS.Data.Tender.ElectronicAuctions(
            details = endedAuctions.auctions.details.map { detail ->
                EndRS.Data.Tender.ElectronicAuctions.Detail(
                    id = detail.id,
                    relatedLot = detail.relatedLot,
                    auctionPeriod = EndRS.Data.Tender.ElectronicAuctions.Detail.AuctionPeriod(
                        startDate = detail.auctionPeriod.startDate,
                        endDate = detail.auctionPeriod.endDate
                    ),
                    electronicAuctionModalities = electronicAuctionModalities(detail.electronicAuctionModalities),
                    electronicAuctionProgress = electronicAuctionProgress(detail.electronicAuctionProgress),
                    electronicAuctionResult = electronicAuctionResult(detail.electronicAuctionResult)
                )
            }
        )

    private fun electronicAuctionModalities(modalities: List<EndedAuctions.Auctions.Detail.ElectronicAuctionModality>): List<EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality> =
        modalities.map { modality ->
            EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                url = modality.url,
                eligibleMinimumDifference = EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                    amount = modality.eligibleMinimumDifference.amount,
                    currency = modality.eligibleMinimumDifference.currency
                )
            )
        }

    private fun electronicAuctionProgress(processes: List<EndedAuctions.Auctions.Detail.ElectronicAuctionProgress>): List<EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress> =
        processes.map { process ->
            EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress(
                id = process.id,
                period = EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress.Period(
                    startDate = process.period.startDate,
                    endDate = process.period.endDate
                ),
                breakdowns = breakdowns(process)
            )
        }

    private fun breakdowns(process: EndedAuctions.Auctions.Detail.ElectronicAuctionProgress): List<EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress.Breakdown> =
        process.breakdowns.map { breakdown ->
            EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress.Breakdown(
                relatedBid = breakdown.relatedBid,
                status = breakdown.status,
                dateMet = breakdown.dateMet,
                value = EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress.Breakdown.Value(
                    amount = breakdown.value.amount,
                    currency = breakdown.value.currency
                )
            )
        }

    private fun electronicAuctionResult(results: List<EndedAuctions.Auctions.Detail.ElectronicAuctionResult>): List<EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionResult> =
        results.map { result ->
            EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionResult(
                relatedBid = result.relatedBid,
                value = EndRS.Data.Tender.ElectronicAuctions.Detail.ElectronicAuctionResult.Value(
                    amount = result.value.amount,
                    currency = result.value.currency
                )
            )
        }
}