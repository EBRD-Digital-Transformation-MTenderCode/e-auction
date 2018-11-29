package com.procurement.auction.exception.command

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.command.name.CommandName
import com.procurement.auction.exception.app.ApplicationException
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class CommandCanNotBeExecutedException(name: CommandName, status: AuctionsStatus) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.COMMAND_CANNOT_BE_EXECUTED,
        message = "The '${name.code}' command cannot be executed. The tender is in '${status.description}' status."
    )