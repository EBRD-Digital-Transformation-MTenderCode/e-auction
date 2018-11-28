package com.procurement.auction.exception.database

import com.procurement.auction.domain.model.version.RowVersion

class OptimisticLockException(readVersion: RowVersion, versionInDatabase: Int) :
    RuntimeException("The read version '${readVersion.original}' of the aggregate does not match the version in the databases '$versionInDatabase'.")