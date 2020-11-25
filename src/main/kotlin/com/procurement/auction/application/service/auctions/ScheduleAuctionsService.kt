package com.procurement.auction.application.service.auctions

import com.procurement.auction.application.auctionDuration
import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.info
import com.procurement.auction.domain.model.auction.EstimatedDurationAuction
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bucket.AuctionsTimes
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.tender.TenderEntity
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.BucketService
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.domain.service.UrlGeneratorService
import com.procurement.auction.exception.app.DuplicateLotException
import com.procurement.auction.exception.app.IncorrectNumberModalitiesException
import com.procurement.auction.exception.app.NoLotsException
import com.procurement.auction.exception.command.CommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.dto.command.ScheduleAuctionsCommand
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate

interface ScheduleAuctionsService {
    fun schedule(command: ScheduleAuctionsCommand): ScheduledAuctionsSnapshot
}

@Service
class ScheduleAuctionsServiceImpl(
    private val auctionProperties: AuctionProperties,
    private val tenderRepository: TenderRepository,
    private val bucketService: BucketService,
    private val deserializer: JsonDeserializeService,
    private val urlGenerator: UrlGeneratorService
) : ScheduleAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
        private const val dateOffsetDays = 1L
    }

    private val durationOneAuction: Duration = auctionDuration(
        durationOneStep = auctionProperties.durationOneStep!!,
        durationPauseAfterStep = auctionProperties.durationPauseAfterStep!!,
        qtyParticipants = auctionProperties.qtyParticipants!!.toLong(),
        qtyRounds = auctionProperties.qtyRounds!!,
        durationPauseAfterAuction = auctionProperties.durationPauseAfterAuction!!
    )

    init {
        log.info { "qty-rounds: ${auctionProperties.qtyRounds}" }
        log.info { "qty-participants: ${auctionProperties.qtyParticipants}" }
        log.info { "duration-one-step: ${auctionProperties.durationOneStep}" }
        log.info { "duration-pause-after-step: ${auctionProperties.durationPauseAfterStep}" }
        log.info { "duration-pause-after-auction: ${auctionProperties.durationPauseAfterAuction}" }
    }

    override fun schedule(command: ScheduleAuctionsCommand): ScheduledAuctionsSnapshot {
        val cpid = command.context.cpid
        val ocid = command.context.ocid
        val entity: TenderEntity? = tenderRepository.loadEntity(cpid, ocid)

        return if (entity != null) {
            when (entity.status) {
                AuctionsStatus.SCHEDULED -> {
                    if (entity.operationId == command.context.operationId)
                        entity.toScheduledAuctionsSnapshot(deserializer)
                    else
                        throw CommandCanNotBeExecutedException(command.name, entity.status)
                }

                AuctionsStatus.CANCELED ->
                    processing(command, entity.rowVersion.next())

                AuctionsStatus.STARTED ->
                    throw CommandCanNotBeExecutedException(command.name, entity.status)

                AuctionsStatus.ENDED ->
                    throw CommandCanNotBeExecutedException(command.name, entity.status)
            }
        } else {
            processing(command, RowVersion.of())
        }
    }

    private fun processing(command: ScheduleAuctionsCommand, rowVersion: RowVersion): ScheduledAuctionsSnapshot {
        validate(command)

        val cpid = command.context.cpid
        val country = command.context.country

        val dateStartAuction = minDateOfStartAuction(command.data.tenderPeriod.endDate.toLocalDate())
        val estimates = estimates(command)
        val auctionsTimes = bucketService.booking(cpid, country, dateStartAuction, estimates)

        return genScheduledAuctions(command = command, auctionsTimes = auctionsTimes, rowVersion = rowVersion)
            .also {
                tenderRepository.save(it)
                log.info { "Scheduled auctions in tender with id '${cpid}'." }
            }
    }

    private fun validate(command: ScheduleAuctionsCommand) {
        if (command.data.electronicAuctions.details.isEmpty())
            throw NoLotsException()

        val uniqueLotIds = mutableSetOf<LotId>()
        for (detail in command.data.electronicAuctions.details) {
            val lotId = detail.relatedLot
            if (!uniqueLotIds.add(lotId))
                throw DuplicateLotException(lotId = lotId)

            if (detail.electronicAuctionModalities.size != 1)
                throw IncorrectNumberModalitiesException(lotId = lotId)
        }
    }

    private fun estimates(command: ScheduleAuctionsCommand): List<EstimatedDurationAuction> {
        return command.data.electronicAuctions.details.map {
            EstimatedDurationAuction(
                lotId = it.relatedLot,
                duration = durationOneAuction
            )
        }
    }

    private fun genScheduledAuctions(
        command: ScheduleAuctionsCommand,
        auctionsTimes: AuctionsTimes,
        rowVersion: RowVersion
    ): ScheduledAuctionsSnapshot {
        val cpid = command.context.cpid
        val country = command.context.country
        val operationId = command.context.operationId

        return ScheduledAuctionsSnapshot(
            rowVersion = rowVersion,
            operationId = operationId,
            cpid = cpid,
            ocid = command.context.ocid,
            data = ScheduledAuctionsSnapshot.Data(
                apiVersion = ScheduledAuctionsSnapshot.apiVersion,
                tender = ScheduledAuctionsSnapshot.Data.Tender(
                    id = cpid,
                    country = country,
                    status = AuctionsStatus.SCHEDULED,
                    startDate = auctionsTimes.startDateTime
                ),
                slots = auctionsTimes.slotsIds.toSet(),
                auctions = command.data.electronicAuctions.details.map { detail ->
                    val lotId = detail.relatedLot
                    val startDateTime = auctionsTimes.items[lotId]!!

                    ScheduledAuctionsSnapshot.Data.Auction(
                        id = detail.id,
                        lotId = lotId,
                        auctionPeriod = ScheduledAuctionsSnapshot.Data.Auction.AuctionPeriod(
                            startDate = startDateTime
                        ),
                        modalities = detail.electronicAuctionModalities.map { modality ->
                            ScheduledAuctionsSnapshot.Data.Auction.Modality(
                                url = urlGenerator.forModality(cpid = command.context.cpid, relatedLot = lotId),
                                eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
                                    ScheduledAuctionsSnapshot.Data.Auction.Modality.EligibleMinimumDifference(
                                        amount = emd.amount,
                                        currency = emd.currency
                                    )
                                }
                            )
                        }
                    )
                }
            )
        )
    }

    private fun minDateOfStartAuction(endDate: LocalDate): LocalDate = endDate.plusDays(dateOffsetDays)
}