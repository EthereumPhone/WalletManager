package com.core.data.repository


interface SwapRepository {

    suspend fun getQuote(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double,
        receiverAddress: String,
        chainId: Int
    ): Double
    suspend fun swap(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double
    ): String
}