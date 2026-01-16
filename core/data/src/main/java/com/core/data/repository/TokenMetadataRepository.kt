package com.core.data.repository

import com.core.database.model.erc20.TokenMetadataEntity
import com.core.model.NetworkChain
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow

interface TokenMetadataRepository {
    fun getTokensMetadata(): Flow<List<TokenMetadata>>
    fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>>
    fun getTokensMetadataBySymbols(symbols: List<String>): Flow<List<TokenMetadata>>
    fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>>
    suspend fun refreshTokensMetadata(
        contractAddresses: List<String>,
        chainId: Int
    )
    suspend fun refreshTokensMetadataByNetwork(contractAddresses: List<String>, network: NetworkChain)

    suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>)

    /**
     * Ensures that tokens across chains are assigned to the correct cross-chain group.
     * This reconciles existing rows where groupId may have been created per-chain.
     */
    suspend fun reconcileTokenGroups()

    /**
     * Look up a token by contract address and chain ID.
     * First checks the local database, then falls back to on-chain lookup.
     * Returns null if the token cannot be found.
     *
     * @param contractAddress The contract address to look up
     * @param chainId The chain ID to look up on
     * @return TokenMetadata if found, null otherwise
     */
    suspend fun lookupTokenByAddress(contractAddress: String, chainId: Int): TokenMetadata?

}