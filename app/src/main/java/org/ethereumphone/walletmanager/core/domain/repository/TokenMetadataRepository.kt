package org.ethereumphone.walletmanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata
import org.ethereumphone.walletmanager.core.util.Resource

interface TokenMetadataRepository {
    fun getTokens(): Flow<Resource<List<TokenMetadata>>>
    fun insertAllToken(tokens: List<TokenMetadata>)
    fun getTokenMetadataBySymbol(symbol: String): Flow<Resource<TokenMetadata>>
    fun getTokenMetadataByDecimals(decimals: Int): Flow<Resource<TokenMetadata>>
    fun getTokenMetadataByName(name: String): Flow<Resource<TokenMetadata>>
}