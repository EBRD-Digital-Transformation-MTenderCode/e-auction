package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class DuplicateLotException(lotId: LotId) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.DUPLICATE_LOT,
    message = "Duplicate lot with id: '${lotId.value}'."
)