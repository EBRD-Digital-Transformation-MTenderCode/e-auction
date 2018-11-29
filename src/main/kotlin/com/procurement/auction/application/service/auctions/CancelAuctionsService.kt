package com.procurement.auction.application.service.auctions

import com.procurement.auction.domain.command.CancelAuctionsCommand
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.info
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.tender.snapshot.CancelledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.BucketService
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.exception.app.TenderNotFoundException
import com.procurement.auction.exception.command.CommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service

interface CancelAuctionsService {
    fun cancel(command: CancelAuctionsCommand): CancelledAuctionsSnapshot
}

@Service
class CancelAuctionsServiceImpl(
    private val bucketService: BucketService,
    private val tenderRepository: TenderRepository,
    private val deserializer: JsonDeserializeService
) : CancelAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun cancel(command: CancelAuctionsCommand): CancelledAuctionsSnapshot {
        val cpid = command.context.cpid
        val entity = tenderRepository.loadEntity(cpid)
            ?: throw TenderNotFoundException(cpid)

        return when (entity.status) {
            AuctionsStatus.SCHEDULED ->
                processing(command, entity.toScheduledAuctionsSnapshot(deserializer))

            AuctionsStatus.CANCELED -> {
                if (entity.operationId == command.context.operationId)
                    entity.toCancelledAuctionsSnapshot(deserializer)
                else
                    throw CommandCanNotBeExecutedException(command.name, entity.status)
            }

            AuctionsStatus.STARTED ->
                throw CommandCanNotBeExecutedException(command.name, entity.status)

            AuctionsStatus.ENDED ->
                throw CommandCanNotBeExecutedException(command.name, entity.status)
        }
    }

    private fun processing(command: CancelAuctionsCommand,
                           snapshot: ScheduledAuctionsSnapshot): CancelledAuctionsSnapshot {

        return cancelledAuctions(command, snapshot)
            .also {
                tenderRepository.save(it)

                val id = snapshot.data.tender.id
                log.info { "Ended auctions in tender with id: '$id'." }

                val startDate = snapshot.data.tender.startDate.toLocalDate()
                val country = snapshot.data.tender.country
                val bucketId = BucketId(startDate, country)

                val slotsIds = snapshot.data.slots.toSet()
                bucketService.release(cpid = id, id = bucketId, slotsIds = slotsIds)
                log.info { "Released slots: '$slotsIds' in bucket with id: '$bucketId' on the tender with id: '$id'." }
            }
    }

    private fun cancelledAuctions(command: CancelAuctionsCommand,
                                  snapshot: ScheduledAuctionsSnapshot): CancelledAuctionsSnapshot {
        return CancelledAuctionsSnapshot(
            rowVersion = snapshot.rowVersion.next(),
            operationId = command.context.operationId,
            data = CancelledAuctionsSnapshot.Data(
                apiVersion = CancelledAuctionsSnapshot.apiVersion,
                tender = CancelledAuctionsSnapshot.Data.Tender(
                    id = snapshot.data.tender.id,
                    country = snapshot.data.tender.country,
                    status = AuctionsStatus.CANCELED
                )
            )
        )
    }
}