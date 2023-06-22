package com.core.domain

import com.core.Resource.Resource
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetTokenBalanceWithMetadata(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val walletSDK: WalletSDK
    ) {

    operator fun invoke(
        fetchRemote: Boolean,
        address: String,
    ): Flow<Resource<TokenAsset>> = combine(
        tokenBalanceRepository.getTokenBalanceByContractAddress(
            fetchRemote,

        )
    )
}