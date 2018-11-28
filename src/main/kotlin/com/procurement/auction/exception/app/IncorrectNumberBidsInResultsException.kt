package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class IncorrectNumberBidsInResultsException(auctionId: String, lotId: LotId) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.INCORRECT_NUMBER_BIDS_IN_RESULT,
        message = "The auction with id: '$auctionId' by lot '${lotId.value}' in results contains the incorrect number of bids."
    )