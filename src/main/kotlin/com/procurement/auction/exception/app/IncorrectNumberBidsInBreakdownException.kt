package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.progressId.ProgressId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class IncorrectNumberBidsInBreakdownException(auctionId: String, lotId: LotId, offerId: ProgressId) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.INCORRECT_NUMBER_BIDS_IN_BREAKDOWN,
        message = "The auction with id: '$auctionId' by lot '${lotId.value}' in breakdown with id: '${offerId.value}' contains the incorrect number of bids."
    )