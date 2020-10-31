package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.infrastructure.web.dispatcher.CodesOfErrors

class TenderNotFoundException(cpid: CPID) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.TENDER_NOT_FOUND,
        message = "Auctions by the tender id: '$cpid' not found."
    )