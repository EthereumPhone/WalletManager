package com.core.data.service

import com.core.database.model.erc20.TokenMetadataEntity

/**
 * Interface for fetching token metadata from the blockchain
 */
interface TokenMetadataFetcher {
    /**
     * Fetches token metadata from the blockchain for a given contract address and chain
     * Returns null if the contract is not an ERC20 token or if fetching fails
     */
    suspend fun fetchTokenMetadata(
        contractAddress: String,
        chainId: Int,
        rpcUrl: String
    ): TokenMetadataEntity?
}
