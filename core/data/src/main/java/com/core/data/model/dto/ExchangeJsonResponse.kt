package com.core.data.model.dto

data class ExchangeJsonResponse(
    val data: Data
)

data class Data(
    val amount: String,
    val base: String,
    val currency: String
)