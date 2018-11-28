package com.procurement.auction.domain.model.bucket

import com.procurement.auction.AbstractBase
import com.procurement.auction.toJson
import com.procurement.auction.toObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BucketSnapshotTest : AbstractBase() {
    @Test
    fun slots() {
        val json = RESOURCES.load("json/domain/model/bucket/snapshot/slots.json")
        val obj = mapper.toObject<BucketSnapshot.SlotsSnapshot>(json)
        Assertions.assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        Assertions.assertEquals(json, jsonFromObj)
    }

    @Test
    fun occupancy() {
        val json = RESOURCES.load("json/domain/model/bucket/snapshot/occupancy.json")
        val obj = mapper.toObject<BucketSnapshot.OccupancySnapshot>(json)
        Assertions.assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        Assertions.assertEquals(json, jsonFromObj)
    }
}