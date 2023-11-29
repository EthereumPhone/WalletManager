package com.core.data.repository

import com.core.model.TokenAsset

interface Erc20TransferRepository {
    suspend fun sendErc20Token(
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    )

}