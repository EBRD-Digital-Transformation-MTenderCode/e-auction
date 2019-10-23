package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class DuplicateLotException private constructor(message: String) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.DUPLICATE_LOT,
    message = message
) {
    constructor(lotId: LotId) : this(message = "Duplicate lot with id: '${lotId.value}'.")

    constructor() : this(message = "Duplicate lot id.")
}
