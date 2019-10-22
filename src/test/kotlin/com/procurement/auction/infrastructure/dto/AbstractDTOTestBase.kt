package com.procurement.auction.infrastructure.dto

import com.procurement.auction.infrastructure.json.testingBindingAndMapping

abstract class AbstractDTOTestBase<T : Any>(private val target: Class<T>) {
    fun testBindingAndMapping(pathToJsonFile: String) {
        testingBindingAndMapping(pathToJsonFile, target)
    }
}
