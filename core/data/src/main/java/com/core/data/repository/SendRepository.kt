package com.core.data.repository

import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface SendRepository {

    val currentTransactionHash: Flow<String>
    val currentTransactionChainId: Flow<Int>
    suspend fun transferEth(
        chainId: Int,
        toAddress: String,
        value: String,
        data: String?,
        gasPrice: String? = null,
        gasAmount: String = ""
    )

    suspend fun transferErc20(
        chainId: Int,
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    )

    suspend fun maxAllowedSend(
        amount: BigDecimal,
        chainId: Int
    ): String

    /**
     * Returns the precise ERC20 token balance for the given contract and chain as a human-readable
     * string, scaled by [decimals] without rounding up (never exceeds on-chain balance).
     */
    suspend fun getMaxErc20AmountString(
        contractAddress: String,
        chainId: Int,
        decimals: Int
    ): String

    fun restoreState()
}