package com.core.data.repository

import com.core.model.TokenAsset
import java.math.BigDecimal

interface SendRepository {
    suspend fun transferEth(
        chainId: Int,
        toAddress: String,
        value: String,
        data: String?,
        gasPrice: String? = null,
        gasAmount: String = ""
    )

    suspend fun transferErc20(
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    )

    suspend fun maxAllowedSend(
        amount: BigDecimal,
        chainId: Int
    ): String
}