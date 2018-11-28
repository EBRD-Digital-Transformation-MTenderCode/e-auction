package com.procurement.auction.application.service.auctions

import com.procurement.auction.domain.command.CancelAuctionsCommand
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.BucketService
import com.procurement.auction.exception.app.TenderNotFoundException
import com.procurement.auction.exception.command.CancelCommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service

interface CancelAuctionsService {
    fun cancel(command: CancelAuctionsCommand): TenderSnapshot
}

@Service
class CancelAuctionsServiceImpl(
    private val bucketService: BucketService,
    private val tenderRepository: TenderRepository
) : CancelAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun cancel(command: CancelAuctionsCommand): TenderSnapshot {
        val cpid = command.context.cpid
        val tender = tenderRepository.load(cpid)
            ?: throw TenderNotFoundException(cpid)

        if (!tender.canCancel) {
            if (tender.auctionsStatus == AuctionsStatus.CANCELED && tender.operationId == command.context.operationId)
                return tender.toSnapshot()
            else
                throw CancelCommandCanNotBeExecutedException("The '${command.name.code}' command cannot be executed. The tender is in '${tender.auctionsStatus.description}' status.")
        }

        val startDate = tender.startDate!!.toLocalDate()
        val slotsIds = tender.slots.toSet()

        tender.cancelAuctions(command.context.operationId)
        tenderRepository.saveCancelledAuctions(cpid, tender)
        log.debug { "Ended auctions in tender with cpid: '$cpid'." }

        val bucketId = BucketId(startDate, tender.country)
        bucketService.release(cpid = cpid, id = bucketId, slotsIds = slotsIds)
        log.debug { "Released slots: '$slotsIds' in bucket with id: '$bucketId' on the tender with id: '$cpid'." }
        return tender.toSnapshot()
    }
}