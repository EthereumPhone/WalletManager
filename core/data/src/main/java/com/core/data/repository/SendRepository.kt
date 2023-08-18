package com.core.data.repository

import java.math.BigDecimal

interface SendRepository {
    suspend fun sendTo(
        chainId: Int,
        toAddress: String,
        value: String,
        data: String?,
        gasPrice: String? = null,
        gasAmount: String = ""
    )

    suspend fun maxAllowedSend(
        amount: BigDecimal,
        chainId: Int
    ): String
}