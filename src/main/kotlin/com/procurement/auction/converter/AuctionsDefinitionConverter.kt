package com.procurement.auction.converter

import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.domain.request.schedule.ScheduleRQ
import com.procurement.auction.domain.schedule.AuctionsDefinition

interface AuctionsDefinitionConverter : Converter<ScheduleRQ, AuctionsDefinition>

class AuctionsDefinitionConverterImpl(private val auctionProperties: AuctionProperties) : AuctionsDefinitionConverter {

    override fun convert(source: ScheduleRQ): AuctionsDefinition {
        val context = source.context
        val cpid = context.cpid
        val country = context.country
        val data = source.data

        return AuctionsDefinition(
            cpid = cpid,
            country = country,
            tenderPeriodEnd = data.tenderPeriod.endDate.toLocalDate(),
            details = data.electronicAuctions.details.map { detail ->
                AuctionsDefinition.Detail(
                    relatedLot = detail.relatedLot,
                    duration = auctionProperties.durationOneAuction,
                    electronicAuctionModalities = detail.electronicAuctionModalities
                        .map { electronicAuctionModality ->
                            AuctionsDefinition.Detail.ElectronicAuctionModality(
                                eligibleMinimumDifference = AuctionsDefinition.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                    amount = electronicAuctionModality.eligibleMinimumDifference.amount,
                                    currency = electronicAuctionModality.eligibleMinimumDifference.currency
                                )
                            )
                        }
                )
            }
        )
    }
}