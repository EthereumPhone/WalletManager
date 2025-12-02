package com.example.transactions.fakes

import com.core.data.repository.TokenMetadataRepository
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.model.NetworkChain
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTokenMetadataRepository(
    initialMetadata: List<TokenMetadata> = emptyList()
) : TokenMetadataRepository {

    private val metadataState = MutableStateFlow(initialMetadata)

    override fun getTokensMetadata(): Flow<List<TokenMetadata>> = metadataState.asStateFlow()

    override fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>> =
        metadataState.asStateFlow()

    override fun getTokensMetadataBySymbols(symbols: List<String>): Flow<List<TokenMetadata>> =
        metadataState.asStateFlow()

    override fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>> =
        metadataState.asStateFlow()

    override suspend fun refreshTokensMetadata(
        contractAddresses: List<String>,
        chainId: Int
    ) {
        // no-op for tests
    }

    override suspend fun refreshTokensMetadataByNetwork(
        contractAddresses: List<String>,
        network: NetworkChain
    ) {
        // no-op for tests
    }

    override suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>) {
        // For tests, just map to domain model
        metadataState.value = tokensMetadata.map {
            TokenMetadata(
                contractAddress = it.contractAddress,
                decimals = it.decimals,
                name = it.name,
                symbol = it.symbol,
                logo = it.logo,
                chainId = it.chainId,
                swappable = false
            )
        }
    }

    override suspend fun reconcileTokenGroups() {
        // no-op for tests
    }

    fun emitMetadata(metadata: List<TokenMetadata>) {
        metadataState.value = metadata
    }
}




