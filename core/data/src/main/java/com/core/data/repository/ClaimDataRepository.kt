package com.core.data.repository

import android.util.Log
import com.core.data.model.dto.ClaimDataResponse
import com.core.data.model.dto.ClaimToken
import com.core.data.remote.ClaimDataApi
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.TokenMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching claim data and seeding token metadata.
 * This ensures tokens from the claim API appear in the wallet if the user has a balance.
 */
interface ClaimDataRepository {
    /**
     * Fetches claim data from the API and seeds token metadata into the database.
     * This should be called periodically or on app startup.
     */
    suspend fun refreshClaimTokens()
    
    /**
     * Gets the current claim data from the API
     */
    suspend fun getClaimData(): ClaimDataResponse?
}

@Singleton
class DefaultClaimDataRepository @Inject constructor(
    private val claimDataApi: ClaimDataApi,
    private val tokenMetadataDao: TokenMetadataDao
) : ClaimDataRepository {
    
    companion object {
        private const val TAG = "ClaimDataRepository"
    }
    
    override suspend fun refreshClaimTokens() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching claim data from API...")
                val claimData = claimDataApi.getClaimData()
                
                val tokenEntities = mutableListOf<TokenMetadataEntity>()
                
                claimData.chains.forEach { chain ->
                    Log.d(TAG, "Processing ${chain.tokens.size} tokens for ${chain.name} (chainId: ${chain.chainId})")
                    
                    chain.tokens.forEach { token ->
                        tokenEntities.add(token.toMetadataEntity(chain.chainId))
                    }
                }
                
                if (tokenEntities.isNotEmpty()) {
                    Log.d(TAG, "Seeding ${tokenEntities.size} claim tokens into database")
                    tokenMetadataDao.upsertTokensMetadata(tokenEntities)
                    Log.d(TAG, "Successfully seeded claim tokens")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching/seeding claim tokens", e)
                // Don't crash - this is a supplementary feature
            }
        }
    }
    
    override suspend fun getClaimData(): ClaimDataResponse? = withContext(Dispatchers.IO) {
        try {
            claimDataApi.getClaimData()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching claim data", e)
            null
        }
    }
    
    /**
     * Converts a ClaimToken to a TokenMetadataEntity for database storage
     */
    private fun ClaimToken.toMetadataEntity(chainId: Int): TokenMetadataEntity {
        return TokenMetadataEntity(
            contractAddress = address.lowercase(),
            chainId = chainId,
            decimals = decimals,
            name = name,
            symbol = symbol,
            logo = null, // Claim tokens don't have logos from the API
            swappable = false, // Mark as not swappable by default
            groupId = null // No group association for claim tokens
        )
    }
}

