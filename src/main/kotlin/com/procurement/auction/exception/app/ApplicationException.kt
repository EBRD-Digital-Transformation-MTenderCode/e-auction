package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.dispatcher.CodeError

abstract class ApplicationException(val loglevel: Logger.Level,
                                    val codeError: CodeError,
                                    message: String,
                                    cause: Throwable? = null) :
    RuntimeException(message, cause)