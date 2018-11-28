package com.procurement.auction.application.service.auctions

import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.domain.command.ScheduleAuctionsCommand
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.auction.EstimatedDurationAuction
import com.procurement.auction.domain.model.auction.ScheduledAuction
import com.procurement.auction.domain.model.auction.id.AuctionId
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bucket.AuctionsTimes
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.modality.Modality
import com.procurement.auction.domain.model.tender.Tender
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.model.value.Value
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.BucketService
import com.procurement.auction.exception.app.DuplicateLotException
import com.procurement.auction.exception.app.IncorrectNumberModalitiesException
import com.procurement.auction.exception.app.NoLotsException
import com.procurement.auction.exception.command.ScheduleCommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDate

interface ScheduleAuctionsService {
    fun schedule(command: ScheduleAuctionsCommand): TenderSnapshot
}

@Service
class ScheduleAuctionsServiceImpl(
    private val auctionProperties: AuctionProperties,
    private val tenderRepository: TenderRepository,
    private val bucketService: BucketService
) : ScheduleAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
        private const val dateOffsetDays = 1L
    }

    private val urlAuction: String = genUrlAuctions()

    override fun schedule(command: ScheduleAuctionsCommand): TenderSnapshot {
        val cpid = command.context.cpid
        val country = command.context.country

        val tender = tenderRepository.load(cpid)
            ?.also {
                log.debug { "Read tender ($it)" }
            } ?: Tender.of(cpid, country)
            .also {
                log.debug { "Created new tender ($it)" }
            }

        if (!tender.canSchedule) {
            if (tender.auctionsStatus == AuctionsStatus.SCHEDULED && tender.operationId == command.context.operationId)
                return tender.toSnapshot()
            else
                throw ScheduleCommandCanNotBeExecutedException("The '${command.name.code}' command cannot be executed. The tender is in '${tender.auctionsStatus.description}' status.")
        }

        validate(command)

        val dateStartAuction = minDateOfStartAuction(command.data.tenderPeriod.endDate.toLocalDate())
        val estimates = estimates(command)
        val auctionsTimes = bucketService.booking(cpid, country, dateStartAuction, estimates)

        val scheduledAuctions = genScheduledAuctions(command, auctionsTimes)
        tender.scheduleAuctions(
            operationId = command.context.operationId,
            startDate = auctionsTimes.startDateTime,
            slots = auctionsTimes.slotsIds,
            auctions = scheduledAuctions
        )

        tenderRepository.saveScheduledAuctions(cpid, tender)
        log.debug { "Scheduled auctions in tender with $cpid." }
        return tender.toSnapshot()
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
                duration = auctionProperties.durationOneAuction
            )
        }
    }

    private fun genScheduledAuctions(command: ScheduleAuctionsCommand,
                                     auctionsTimes: AuctionsTimes): List<ScheduledAuction> {
        return command.data.electronicAuctions.details.map { detail ->
            val lotId = detail.relatedLot
            val startDateTime = auctionsTimes.items[lotId]!!

            ScheduledAuction(
                id = AuctionId(),
                lotId = lotId,
                startDate = startDateTime,
                status = AuctionsStatus.SCHEDULED,
                modalities = detail.electronicAuctionModalities.map { modality ->
                    Modality(
                        url = genUrl(cpid = command.context.cpid, relatedLot = lotId),
                        eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
                            Value(
                                amount = emd.amount,
                                currency = emd.currency
                            )
                        }
                    )
                }
            )

        }
    }

    private fun minDateOfStartAuction(endDate: LocalDate): LocalDate = endDate.plusDays(dateOffsetDays)

    private fun genUrl(cpid: CPID, relatedLot: LotId): String = "$urlAuction/auctions/${cpid.value}/${relatedLot.value}"

    private fun genUrlAuctions(): String {
        val url = auctionProperties.url
            ?: throw IllegalStateException("Not set the url of an tender.")
        val protocol = url.protocol
            ?: throw IllegalStateException("Not set the scheme of the url.")
        val host = url.host
            ?: throw IllegalStateException("Not set the domain name of the url.")
        return getUrlASCII("$protocol://$host")
    }

    private fun getUrlASCII(uri: String): String = try {
        URL(uri).toURI().toASCIIString()
    } catch (exception: Exception) {
        throw IllegalStateException("Invalid the tender url: '$uri'.")
    }
}