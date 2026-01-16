package com.core.domain

import com.core.data.repository.DEFAULT_EXCLUDE_LIST
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.UserDataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Use case to get a comprehensive list of tokens available for swap selection.
 * 
 * Includes:
 * - Tokens the user owns (with balances)
 * - All swappable tokens from metadata (even if user doesn't own them)
 * 
 * Filters:
 * - Excludes the currently selected "FROM" token
 * - Filters by target chain ID (or all supported chains if targetChainId is null and allChains is true)
 * - Applies search query
 * - Shows owned tokens first, sorted by balance
 */
class GetSwappableTokensForSelection @Inject constructor(
    private val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
    private val getSwapTokens: GetSwapTokens,
    private val groupedTokenRepository: GroupedTokenRepository,
    private val userDataRepository: UserDataRepository
) {
    // Supported chains for cross-chain swaps
    private val supportedChainIds = listOf(1, 10, 137, 42161, 8453)
    
    operator fun invoke(
        excludeToken: TokenAsset? = null,
        targetChainId: Int? = null,
        query: String = "",
        allChains: Boolean = false // New parameter to fetch from all chains
    ): Flow<List<TokenAsset>> = flow {
        val effectiveChainId = targetChainId ?: userDataRepository.userData.first().walletNetwork.toInt()
        
        // If allChains is true, fetch tokens from all supported chains
        val chainIds = if (allChains) supportedChainIds else listOf(effectiveChainId)
        
        combine(
            // Get all owned token groups
            getAllGroupedTokensUsecase(DEFAULT_EXCLUDE_LIST),
            // Get all swappable tokens for the chain(s)
            if (allChains) {
                // Combine tokens from all supported chains
                flow {
                    val allSwappableTokens = mutableListOf<TokenAsset>()
                    for (chainId in supportedChainIds) {
                        getSwapTokens(query, chainId).first().let { tokens ->
                            allSwappableTokens.addAll(tokens)
                        }
                    }
                    emit(allSwappableTokens)
                }
            } else {
                getSwapTokens(query, effectiveChainId)
            }
        ) { ownedGroups, swappableTokens ->
            
            // Convert owned groups to individual TokenAssets on the target chain(s)
            val ownedTokensOnChain = mutableListOf<TokenAsset>()
            
            for (group in ownedGroups) {
                try {
                    val tokensInGroup = groupedTokenRepository
                        .observeAllTokensWithPriceInGroup(group.groupId, filterZeroBalance = false)
                        .first()
                        .filter { token -> 
                            token.swappable && (allChains || token.chainId == effectiveChainId)
                        }
                    
                    tokensInGroup.forEach { token ->
                        ownedTokensOnChain.add(
                            TokenAsset(
                                address = token.address,
                                chainId = token.chainId,
                                symbol = token.symbol,
                                name = token.name,
                                balance = token.balance,
                                decimals = token.decimals,
                                logoUrl = token.logoUrl,
                                swappable = token.swappable
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip groups that fail to load
                    continue
                }
            }
            
            // Combine owned + all swappable tokens
            val allTokens = (ownedTokensOnChain + swappableTokens)
                .distinctBy { "${it.address.lowercase()}_${it.chainId}" }
                .filter { token ->
                    // Exclude currently selected FROM token
                    excludeToken?.let { excluded ->
                        !(token.address.equals(excluded.address, ignoreCase = true) && 
                          token.chainId == excluded.chainId)
                    } ?: true
                }
                .filter { token ->
                    // Query filtering (already applied in getSwapTokens, but double-check)
                    if (query.isBlank()) true
                    else token.name.contains(query, ignoreCase = true) || 
                         token.symbol.contains(query, ignoreCase = true)
                }
                .sortedWith(
                    compareByDescending<TokenAsset> { it.balance > 0.0 } // Owned tokens first
                        .thenByDescending { it.balance } // Then by balance amount
                        .thenBy { it.symbol } // Then alphabetically
                )
            
            allTokens
        }.collect { emit(it) }
    }
}


