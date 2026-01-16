package com.core.data.repository

import com.core.data.swap.SocketQuoteResponse
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
     * Get a detailed swap quote from 0x API (same-chain swaps).
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
    
    /**
     * Get a cross-chain swap quote from Socket API.
     * Used when fromChainId != toChainId.
     * 
     * @param fromChainId Source chain ID
     * @param toChainId Destination chain ID
     * @param fromTokenAddress Token address on source chain
     * @param toTokenAddress Token address on destination chain
     * @param amount Amount to swap (human-readable format)
     * @param fromTokenDecimals Decimals of the source token
     * @param toTokenDecimals Decimals of the destination token
     * @param fromTokenSymbol Symbol of source token
     * @param toTokenSymbol Symbol of destination token
     * @return SocketQuoteResponse or null if quote fetch fails
     */
    suspend fun getCrossChainQuote(
        fromChainId: Int,
        toChainId: Int,
        fromTokenAddress: String,
        toTokenAddress: String,
        amount: BigDecimal,
        fromTokenDecimals: Int,
        toTokenDecimals: Int,
        fromTokenSymbol: String,
        toTokenSymbol: String
    ): SocketQuoteResponse?
    
    /**
     * Execute a same-chain swap via 0x API.
     */
    suspend fun swap(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double
    ): String
    
    /**
     * Execute a cross-chain swap via Socket API.
     * Bridges tokens from source chain to destination chain.
     * 
     * @param fromChainId Source chain ID
     * @param toChainId Destination chain ID
     * @param fromTokenAddress Token address on source chain
     * @param toTokenAddress Token address on destination chain
     * @param amount Amount to swap (human-readable)
     * @param fromTokenDecimals Decimals of the source token
     * @param toTokenDecimals Decimals of the destination token
     * @param fromTokenSymbol Symbol of source token
     * @param toTokenSymbol Symbol of destination token
     * @return Transaction hash on success, or error string
     */
    suspend fun crossChainSwap(
        fromChainId: Int,
        toChainId: Int,
        fromTokenAddress: String,
        toTokenAddress: String,
        amount: BigDecimal,
        fromTokenDecimals: Int,
        toTokenDecimals: Int,
        fromTokenSymbol: String,
        toTokenSymbol: String
    ): String
    
    /**
     * Check if a swap is cross-chain based on the chain IDs.
     */
    fun isCrossChainSwap(fromChainId: Int, toChainId: Int): Boolean = fromChainId != toChainId
}