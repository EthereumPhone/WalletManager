package com.core.data.repository

import com.core.data.swap.ZeroXSwapQuoteResponse
import java.math.BigDecimal

interface SwapRepository {

    suspend fun getQuote(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double,
        receiverAddress: String,
        chainId: Int
    ): Double
    
    /**
     * Get a detailed swap quote from 0x API.
     * Returns the full quote response including buyAmount, price, and route information.
     * 
     * @param inputTokenAddress Token address to swap from
     * @param outputTokenAddress Token address to swap to
     * @param amount Amount to swap (human-readable format)
     * @param inputTokenDecimals Decimals of the input token
     * @param outputTokenDecimals Decimals of the output token
     * @param chainId Chain ID for the swap
     * @param inputTokenSymbol Symbol of input token (for ETH detection)
     * @param outputTokenSymbol Symbol of output token (for ETH detection)
     * @return ZeroXSwapQuoteResponse or null if quote fetch fails
     */
    suspend fun getSwapQuote(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: BigDecimal,
        inputTokenDecimals: Int,
        outputTokenDecimals: Int,
        chainId: Int,
        inputTokenSymbol: String,
        outputTokenSymbol: String
    ): ZeroXSwapQuoteResponse?
    
    suspend fun swap(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double
    ): String
}