package com.core.data.repository

import com.core.data.remote.Erc20TransferApi
import com.core.model.TokenAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Web3jErc20TransferRepository @Inject constructor(
    private val erc20TransferApi: Erc20TransferApi

): Erc20TransferRepository {
    override suspend fun sendErc20Token(
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    ) {
        withContext(Dispatchers.IO) {
            erc20TransferApi.sendErc20Token(
                toAddress,
                tokenAsset.address,
                amount,
                tokenAsset.decimals
            )
        }
    }
}