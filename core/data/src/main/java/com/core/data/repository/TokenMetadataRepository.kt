package com.core.data.repository

import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow

interface TokenMetadataRepository {
    fun getTokensMetadata(): Flow<List<TokenMetadata>>
    fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>>
    suspend fun refreshTokensMetadata(
        contractAddresses: List<String>,
        network: Int
    )

}