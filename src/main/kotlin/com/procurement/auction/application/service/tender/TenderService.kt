package com.procurement.auction.application.service.tender

import com.procurement.auction.application.presenter.AuctionsPresenter
import com.procurement.auction.application.service.auctions.CancelAuctionsService
import com.procurement.auction.application.service.auctions.EndAuctionsService
import com.procurement.auction.application.service.auctions.ScheduleAuctionsService
import com.procurement.auction.application.service.auctions.StartAuctionsService
import com.procurement.auction.domain.command.CancelAuctionsCommand
import com.procurement.auction.domain.command.EndAuctionsCommand
import com.procurement.auction.domain.command.ScheduleAuctionsCommand
import com.procurement.auction.domain.command.StartAuctionsCommand
import com.procurement.auction.domain.view.CancelledAuctionsView
import com.procurement.auction.domain.view.EndedAuctionsView
import com.procurement.auction.domain.view.ScheduledAuctionsView
import com.procurement.auction.domain.view.StartedAuctionsView
import org.springframework.stereotype.Service

interface TenderService {
    fun scheduleAuctions(command: ScheduleAuctionsCommand): ScheduledAuctionsView
    fun cancelAuctions(command: CancelAuctionsCommand): CancelledAuctionsView
    fun startAuctions(command: StartAuctionsCommand): StartedAuctionsView
    fun endAuctions(command: EndAuctionsCommand): EndedAuctionsView
}

@Service
class TenderServiceImpl(
    private val scheduleAuctionsService: ScheduleAuctionsService,
    private val cancelAuctionsService: CancelAuctionsService,
    private val startAuctionsService: StartAuctionsService,
    private val endAuctionsService: EndAuctionsService,
    private val presenter: AuctionsPresenter
) : TenderService {

    override fun scheduleAuctions(command: ScheduleAuctionsCommand): ScheduledAuctionsView =
        presenter.presentScheduledAuctions(scheduleAuctionsService.schedule(command))

    override fun cancelAuctions(command: CancelAuctionsCommand): CancelledAuctionsView =
        presenter.presentCancelledAuctions(cancelAuctionsService.cancel(command))

    override fun startAuctions(command: StartAuctionsCommand): StartedAuctionsView {
        return startAuctionsService.start(command)
            ?.let { presenter.presentStartedAuctions(it) }
            ?: presenter.presentNoStartedAuctions()
    }

    override fun endAuctions(command: EndAuctionsCommand): EndedAuctionsView =
        presenter.presentEndedAuctions(endAuctionsService.end(command))
}