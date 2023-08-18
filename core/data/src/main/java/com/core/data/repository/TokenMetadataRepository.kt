package com.core.data.repository

import com.core.database.model.erc20.TokenMetadataEntity
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow

interface TokenMetadataRepository {
    fun getTokensMetadata(): Flow<List<TokenMetadata>>
    fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>>
    fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>>
    suspend fun refreshTokensMetadata(
        contractAddresses: List<String>,
        chainId: Int
    )
    suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>)

}