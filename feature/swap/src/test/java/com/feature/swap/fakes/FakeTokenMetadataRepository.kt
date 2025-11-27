package com.feature.swap.fakes

import com.core.data.repository.TokenMetadataRepository
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.model.NetworkChain
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeTokenMetadataRepository : TokenMetadataRepository {

    private val tokensMetadataState = MutableStateFlow<List<TokenMetadata>>(emptyList())

    override fun getTokensMetadata(): Flow<List<TokenMetadata>> = tokensMetadataState.asStateFlow()

    override fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>> =
        tokensMetadataState.asStateFlow().map { list ->
            val set = contractAddresses.map { it.lowercase() }.toSet()
            list.filter { set.contains(it.contractAddress.lowercase()) }
        }

    override fun getTokensMetadataBySymbols(symbols: List<String>): Flow<List<TokenMetadata>> =
        tokensMetadataState.asStateFlow().map { list ->
            val set = symbols.map { it.lowercase() }.toSet()
            list.filter { set.contains(it.symbol.lowercase()) }
        }

    override fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>> =
        tokensMetadataState.asStateFlow().map { list ->
            list.filter { it.chainId == chainId }
        }

    override suspend fun refreshTokensMetadata(
        contractAddresses: List<String>,
        chainId: Int
    ) {
        // no-op for tests
    }

    override suspend fun refreshTokensMetadataByNetwork(contractAddresses: List<String>, network: NetworkChain) {
        // no-op for tests
    }

    override suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>) {
        // no-op for tests
    }

    override suspend fun reconcileTokenGroups() {
        // no-op for tests
    }

    fun emitTokensMetadata(metadata: List<TokenMetadata>) {
        tokensMetadataState.value = metadata
    }
}


