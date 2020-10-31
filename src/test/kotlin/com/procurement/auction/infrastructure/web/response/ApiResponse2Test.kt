package com.procurement.auction.infrastructure.web.response

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import com.procurement.auction.infrastructure.json.JsonValidator
import com.procurement.auction.infrastructure.json.loadJson
import com.procurement.auction.infrastructure.json.toJson
import com.procurement.auction.infrastructure.web.response.version.ApiVersion2
import org.junit.jupiter.api.Test
import java.util.*

class ApiResponse2Test : AbstractDTOTestBase<ApiSuccessResponse2>(ApiSuccessResponse2::class.java) {

    companion object {
        private const val JSON_RESPONSE_WITH_NO_RESULT = "json/infrastructure/dto/api2/api_response_no_result.json"
        private const val JSON_RESPONSE_WITH_RESULT_LIST = "json/infrastructure/dto/api2/api_response_result_list.json"
        private const val JSON_RESPONSE_WITH_RESULT_OBJECT = "json/infrastructure/dto/api2/api_response_result_object.json"
    }

    @Test
    fun nullResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_NO_RESULT)
        val apiResponse2 = ApiSuccessResponse2(
            version = getApiVersion(),
            result = null,
            id = getId()
        )
        val actualJson = apiResponse2.toJson()
        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun emptyResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_NO_RESULT)
        val apiResponse2 = ApiSuccessResponse2(
            version = getApiVersion(),
            result = emptyList<String>(),
            id = getId()
        )
        val actualJson = apiResponse2.toJson()

        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun listResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_RESULT_LIST)
        val apiResponse2 = ApiSuccessResponse2(
            version = getApiVersion(),
            result = listOf(
                "7b1584b8-5eb0-43d8-ad72-f7c074cc6bac",
                "42211541-4d8c-4d43-a1cd-7242a898e0b4"
            ),
            id = getId()
        )
        val actualJson = apiResponse2.toJson()

        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun objectResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_RESULT_OBJECT)
        val apiResponse2 = ApiSuccessResponse2(
            version = getApiVersion(),
            result = object {
                val first = "first"
            },
            id = getId()
        )
        val actualJson = apiResponse2.toJson()

        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun fullData() {
        testBindingAndMapping(JSON_RESPONSE_WITH_RESULT_LIST)
    }

    private fun getApiVersion() = ApiVersion2(2, 0, 0)
    private fun getId() = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")
}
