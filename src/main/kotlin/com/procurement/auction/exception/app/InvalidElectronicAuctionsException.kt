package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class InvalidElectronicAuctionsException(message: String) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.INVALID_ELECTRONIC_AUCTIONS,
    message = message
)