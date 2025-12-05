package com.core.data.repository

import android.util.Log
import com.core.data.model.dto.ClaimDataResponse
import com.core.data.model.dto.ClaimToken
import com.core.data.remote.ClaimDataApi
import com.core.data.service.OnChainTokenBalanceFetcher
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching claim data and seeding token metadata.
 * This ensures tokens from the claim API appear in the wallet if the user has a balance.
 */
interface ClaimDataRepository {
    /**
     * Fetches claim data from the API, seeds token metadata into the database,
     * and checks on-chain balances for the tokens.
     * 
     * @param walletAddress The user's wallet address to check balances for
     */
    suspend fun refreshClaimTokens(walletAddress: String)
    
    /**
     * Gets the current claim data from the API
     */
    suspend fun getClaimData(): ClaimDataResponse?
}

@Singleton
class DefaultClaimDataRepository @Inject constructor(
    private val claimDataApi: ClaimDataApi,
    private val tokenMetadataDao: TokenMetadataDao,
    private val tokenBalanceDao: TokenBalanceDao,
    private val tokenGroupDao: TokenGroupDao,
    private val onChainTokenBalanceFetcher: OnChainTokenBalanceFetcher
) : ClaimDataRepository {
    
    companion object {
        private const val TAG = "ClaimDataRepository"
    }
    
    override suspend fun refreshClaimTokens(walletAddress: String) {
        if (walletAddress.isBlank()) {
            Log.e(TAG, "Wallet address is blank, aborting")
            return
        }
        
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching claim data from API...")
                val claimData = claimDataApi.getClaimData()
                
                val tokenGroupEntities = mutableListOf<TokenGroupEntity>()
                val tokenMetadataEntities = mutableListOf<TokenMetadataEntity>()
                
                // First, seed the token groups and metadata
                claimData.chains.forEach { chain ->
                    chain.tokens.forEach { token ->
                        val groupId = generateGroupId(chain.chainId, token.address)
                        
                        // Create token group
                        tokenGroupEntities.add(
                            TokenGroupEntity(
                                groupId = groupId,
                                canonicalChainId = chain.chainId,
                                canonicalAddress = token.address.lowercase(),
                                symbol = token.symbol,
                                name = token.name
                            )
                        )
                        
                        // Create metadata with groupId
                        tokenMetadataEntities.add(token.toMetadataEntity(chain.chainId, groupId))
                    }
                }
                
                // Upsert token groups first (due to foreign key constraints)
                if (tokenGroupEntities.isNotEmpty()) {
                    tokenGroupDao.upsertTokenGroups(tokenGroupEntities)
                }
                
                // Then upsert metadata
                if (tokenMetadataEntities.isNotEmpty()) {
                    tokenMetadataDao.upsertTokensMetadata(tokenMetadataEntities)
                }
                
                // Now fetch on-chain balances for all claim tokens
                fetchClaimTokenBalances(walletAddress, claimData)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching/seeding claim tokens", e)
            }
        }
    }
    
    /**
     * Generates a unique group ID for a claim token.
     * Format: "claim_{chainId}_{address}" to avoid conflicts with other tokens
     */
    private fun generateGroupId(chainId: Int, address: String): String {
        return "claim_${chainId}_${address.lowercase()}"
    }
    
    /**
     * Fetches on-chain balances for all tokens in the claim data
     */
    private suspend fun fetchClaimTokenBalances(walletAddress: String, claimData: ClaimDataResponse) {
        supervisorScope {
            val balanceJobs = claimData.chains.map { chain ->
                async {
                    try {
                        val tokenAddresses = chain.tokens.map { it.address }
                        
                        val balances = onChainTokenBalanceFetcher.fetchTokenBalances(
                            walletAddress = walletAddress,
                            contractAddresses = tokenAddresses,
                            rpcUrl = chain.rpcUrl
                        )
                        
                        // Convert to TokenBalanceEntity and filter out null/failed fetches
                        val balanceEntities = balances.mapNotNull { (address, balance) ->
                            if (balance != null && balance > BigDecimal.ZERO) {
                                TokenBalanceEntity(
                                    contractAddress = address.lowercase(),
                                    chainId = chain.chainId,
                                    tokenBalance = balance
                                )
                            } else {
                                null
                            }
                        }
                        
                        if (balanceEntities.isNotEmpty()) {
                            tokenBalanceDao.upsertTokenBalances(balanceEntities)
                        }
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching balances for ${chain.name}", e)
                    }
                }
            }
            
            balanceJobs.awaitAll()
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
    private fun ClaimToken.toMetadataEntity(chainId: Int, groupId: String): TokenMetadataEntity {
        return TokenMetadataEntity(
            contractAddress = address.lowercase(),
            chainId = chainId,
            decimals = decimals,
            name = name,
            symbol = symbol,
            logo = null,
            swappable = false,
            groupId = groupId
        )
    }
}
