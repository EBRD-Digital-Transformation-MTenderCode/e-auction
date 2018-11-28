package com.procurement.auction.domain.repository

import com.procurement.auction.domain.model.bucket.Bucket
import com.procurement.auction.domain.model.bucket.id.BucketId

interface BucketRepository {
    fun load(bucketId: BucketId): Bucket?
    fun save(bucket: Bucket)
}
