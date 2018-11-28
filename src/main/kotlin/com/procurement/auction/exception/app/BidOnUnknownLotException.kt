package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class BidOnUnknownLotException(bidId: BidId, lotId: LotId) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.BID_ON_UNKNOWN_LOT,
    message = "The bid with id: '${bidId.value}' on an unknown lot with id: '${lotId.value}' "
)