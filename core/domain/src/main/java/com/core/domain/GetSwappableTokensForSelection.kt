package com.core.domain

import com.core.data.repository.DEFAULT_EXCLUDE_LIST
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.UserDataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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
 * - Filters by target chain ID
 * - Applies search query
 * - Shows owned tokens first, sorted by balance
 */
class GetSwappableTokensForSelection @Inject constructor(
    private val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
    private val getSwapTokens: GetSwapTokens,
    private val groupedTokenRepository: GroupedTokenRepository,
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(
        excludeToken: TokenAsset? = null,
        targetChainId: Int? = null,
        query: String = ""
    ): Flow<List<TokenAsset>> = flowOf(targetChainId).flatMapLatest { chainId ->
        val effectiveChainId = chainId ?: userDataRepository.userData.first().walletNetwork.toInt()
        
        combine(
            // Get all owned token groups
            getAllGroupedTokensUsecase(DEFAULT_EXCLUDE_LIST),
            // Get all swappable tokens for the chain
            getSwapTokens(query, effectiveChainId)
        ) { ownedGroups, swappableTokens ->
            
            // Convert owned groups to individual TokenAssets on the target chain
            val ownedTokensOnChain = mutableListOf<TokenAsset>()
            
            for (group in ownedGroups) {
                try {
                    val tokensInGroup = groupedTokenRepository
                        .observeAllTokensWithPriceInGroup(group.groupId, filterZeroBalance = false)
                        .first()
                        .filter { token -> token.chainId == effectiveChainId && token.swappable }
                    
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
        }
    }
}

