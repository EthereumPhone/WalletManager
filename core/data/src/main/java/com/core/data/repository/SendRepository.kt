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
}