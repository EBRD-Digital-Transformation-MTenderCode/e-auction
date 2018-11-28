package com.procurement.auction.domain.factory

import com.procurement.auction.domain.model.bucket.Bucket
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.RowVersion

interface BucketFactory {
    fun create(id: BucketId,
               rowVersion: RowVersion,
               apiVersion: ApiVersion,
               slots: String,
               occupancy: String): Bucket
}
