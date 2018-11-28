package com.procurement.auction.exception.command

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.exception.app.ApplicationException
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

abstract class CommandCanNotBeExecutedException(message: String) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.COMMAND_CANNOT_BE_EXECUTED,
        message = message
    )