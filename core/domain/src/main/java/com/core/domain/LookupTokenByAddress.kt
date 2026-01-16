package com.core.domain

import com.core.data.repository.TokenMetadataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case to look up a token by its contract address.
 *
 * This enables the "paste contract address to swap" feature.
 * It first checks the local database for known tokens, then
 * falls back to on-chain lookup if not found.
 *
 * Returns a TokenAsset if found, null otherwise.
 */
class LookupTokenByAddress @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository
) {

    companion object {
        // Supported chains for token lookup
        val SUPPORTED_CHAIN_IDS = listOf(1, 10, 137, 42161, 8453)

        // Native token addresses that shouldn't be looked up on-chain
        private val NATIVE_TOKEN_ADDRESSES = setOf(
            "0x0000000000000000000000000000000000000000",
            "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
        )
    }

    /**
     * Look up a token by contract address on a specific chain.
     *
     * @param contractAddress The contract address to look up (must be a valid 0x address)
     * @param chainId The chain ID to look up on
     * @return TokenAsset if found, null otherwise
     */
    suspend operator fun invoke(
        contractAddress: String,
        chainId: Int
    ): TokenAsset? = withContext(Dispatchers.IO) {
        // Validate input
        if (!isValidContractAddress(contractAddress)) {
            return@withContext null
        }

        if (chainId !in SUPPORTED_CHAIN_IDS) {
            return@withContext null
        }

        // Don't look up native token addresses
        if (contractAddress.lowercase() in NATIVE_TOKEN_ADDRESSES) {
            return@withContext null
        }

        // Use repository's lookupTokenByAddress which handles both local DB and on-chain lookup
        try {
            val tokenMetadata = tokenMetadataRepository.lookupTokenByAddress(contractAddress, chainId)

            if (tokenMetadata != null) {
                return@withContext TokenAsset(
                    address = tokenMetadata.contractAddress,
                    chainId = tokenMetadata.chainId,
                    symbol = tokenMetadata.symbol,
                    name = tokenMetadata.name,
                    balance = 0.0,
                    decimals = tokenMetadata.decimals,
                    logoUrl = tokenMetadata.logo,
                    swappable = true
                )
            }
        } catch (e: Exception) {
            // Token lookup failed
        }

        return@withContext null
    }

    /**
     * Look up a token on all supported chains.
     * Returns the first chain where the token is found.
     *
     * @param contractAddress The contract address to look up
     * @return Pair of (TokenAsset, chainId) if found, null otherwise
     */
    suspend fun lookupOnAllChains(contractAddress: String): Pair<TokenAsset, Int>? =
        withContext(Dispatchers.IO) {
            if (!isValidContractAddress(contractAddress)) {
                return@withContext null
            }

            // Try each supported chain
            for (chainId in SUPPORTED_CHAIN_IDS) {
                val token = invoke(contractAddress, chainId)
                if (token != null) {
                    return@withContext token to chainId
                }
            }

            return@withContext null
        }

    /**
     * Check if a string is a valid Ethereum contract address.
     */
    fun isValidContractAddress(address: String): Boolean {
        if (address.isBlank()) return false
        if (!address.startsWith("0x", ignoreCase = true)) return false
        if (address.length != 42) return false

        // Check if all characters after 0x are valid hex
        val hexPart = address.substring(2)
        return hexPart.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
    }
}
