package com.procurement.auction.application.service.tender

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.info
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.repository.TenderMigrationRepository
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service

interface TenderMigrationService {
    fun migration(cpid: CPID)
}

@Service
class TenderMigrationServiceImpl(
    private val tenderRepository: TenderRepository,
    private val tenderMigrationRepository: TenderMigrationRepository
) : TenderMigrationService {

    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun migration(cpid: CPID) {
        val oldAuctions = tenderMigrationRepository.load(cpid)
        if (oldAuctions.isEmpty())
            throw IllegalStateException("Auctions by cpid: '${cpid.value}' not found.")

        log.info { "Auctions by cpid: '${cpid.value}' was found." }

        val newAuctions = oldAuctions.maxBy {
            it.operationDate
        }!!.let { auctions ->
            ScheduledAuctionsSnapshot(
                rowVersion = RowVersion.of(),
                operationId = auctions.operationId,
                data = ScheduledAuctionsSnapshot.Data(
                    apiVersion = ScheduledAuctionsSnapshot.apiVersion,
                    tender = ScheduledAuctionsSnapshot.Data.Tender(
                        id = auctions.cpid,
                        country = Country("MD"),
                        status = AuctionsStatus.SCHEDULED,
                        startDate = auctions.data.startDate
                    ),
                    slots = emptySet(),
                    auctions = auctions.data.lots.map { (textLotId, auction) ->

                        ScheduledAuctionsSnapshot.Data.Auction(
                            id = auction.id,
                            lotId = LotIdDeserializer.deserialize(textLotId),
                            auctionPeriod = ScheduledAuctionsSnapshot.Data.Auction.AuctionPeriod(
                                startDate = auction.startDate
                            ),
                            modalities = listOf(
                                ScheduledAuctionsSnapshot.Data.Auction.Modality(
                                    url = auction.url,
                                    eligibleMinimumDifference = ScheduledAuctionsSnapshot.Data.Auction.Modality.EligibleMinimumDifference(
                                        amount = auction.amount,
                                        currency = auction.currency
                                    )
                                )
                            )
                        )
                    }
                )
            )
        }

        tenderRepository.save(newAuctions)
        log.info { "New version auctions by cpid: '${cpid.value}' was saved." }
    }
}