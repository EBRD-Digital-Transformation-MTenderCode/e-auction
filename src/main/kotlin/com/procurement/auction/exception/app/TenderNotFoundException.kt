package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.infrastructure.web.dispatcher.CodesOfErrors

class TenderNotFoundException(cpid: Cpid, ocid: Ocid) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.TENDER_NOT_FOUND,
        message = "Auctions for the tender by cpid '$cpid' and ocid '$ocid' not found."
    )