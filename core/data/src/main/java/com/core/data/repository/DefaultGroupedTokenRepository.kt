package com.core.data.repository

import com.core.database.dao.TokenGroupDao
import com.core.database.model.erc20.toExternalModelWithPrice
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenGroupAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.TokenGroupAssetWithExchange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.util.Log
import com.core.database.model.erc20.CompositeTokenGroupWithExchange

class DefaultGroupedTokenRepository @Inject constructor(
    val tokenGroupDao: TokenGroupDao
): GroupedTokenRepository {
    override fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>> =
        tokenGroupDao.observeAllActiveTokenGroupsWithExchange()
            .map { groups ->
                // First filter out network tokens (those with groupIds starting with "network_")
                val nonNetworkGroups = groups.filter { !it.tokenGroup.groupId.startsWith("network_") }
                
                val filteredGroups = if (!filterList.isNullOrEmpty()) {
                    nonNetworkGroups.filter { token -> // Filter out tokens with URLs in their names or symbols
                        val name = token.tokenGroup.name
                        val symbol = token.tokenGroup.symbol

                        val containsNoUrlPatterns = filterList.none { pattern ->
                            name.contains(pattern) || symbol.contains(pattern)
                        }
                        containsNoUrlPatterns
                    }
                } else {
                    nonNetworkGroups
                }


                filteredGroups.map {
                    TokenGroupAssetOverview(
                        groupId = it.tokenGroup.groupId,
                        symbol = it.tokenGroup.symbol,
                        name = it.tokenGroup.name,
                        logoUrl = it.logoUrl,
                        totalBalance = it.totalBalance.toDouble(),
                        formattedBalance = it.formattedTotalBalance,
                        totalFiatBalance = it.totalBalanceInUsd,
                        formattedFiatBalance = it.formattedUsdBalance,
                        exchangeCurrency = "usd",
                    )
                }
            }


    override fun observeGroupedTokens(): Flow<List<TokenGroupAsset>> {
        TODO("Not yet implemented")
    }

    override fun observeGroupTokensWithExchange(): Flow<List<TokenGroupAssetWithExchange>> =
        tokenGroupDao.observeAllTokenGroupsWithExchange().map { group -> group.map{ it.toExternalModelWithPrice() } }


    override fun observeAllTokensWithPriceInGroup(groupId: String, filterZeroBalance: Boolean): Flow<List<TokenAssetWithPrice>> {
        // Return empty flow if groupId is empty
        if (groupId.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        
        return tokenGroupDao.observeAllTokensInGroupWithLatestExchange(groupId).map { tokens ->
            // Deduplicate tokens by address+chainId to fix Room @Relation bug
            // where it matches balances only by address, ignoring chainId
            val dedupedTokens = tokens.distinctBy { 
                "${it.contractAddress.lowercase()}_${it.chainId}" 
            }
            
            val mappedTokens = if (groupId.startsWith("network_")) {
                // For network tokens, balance is already in ETH/MATIC units, not wei
                dedupedTokens.mapNotNull { token ->
                    val metadata = token.tokenMetadataEntity
                    val balance = token.tokenBalanceEntity?.tokenBalance?.toDouble() ?: 0.0
                    
                    // Skip tokens without metadata
                    if (metadata == null) {
                        return@mapNotNull null
                    }
                    
                    // Log exchange rate info for debugging
                    if (groupId.startsWith("network_")) {
                        Log.d("DefaultGroupedTokenRepository", 
                            "Network token ${metadata.symbol} on chain ${metadata.chainId}: " +
                            "balance=$balance, exchange=${token.latestExchangeEntity?.value}")
                    }

                    TokenAssetWithPrice(
                        address = metadata.contractAddress,
                        chainId = metadata.chainId,
                        symbol = metadata.symbol,
                        name = metadata.name,
                        balance = balance, // Already in ETH/MATIC units
                        decimals = metadata.decimals,
                        logoUrl = metadata.logo,
                        swappable = metadata.swappable,
                        fiatAmount = if (token.latestExchangeEntity != null) {
                            balance * token.latestExchangeEntity!!.value
                        } else {
                            0.0
                        }
                    )
                }
            } else {
                dedupedTokens.map { it.toExternalModelWithPrice() }
            }
            
            // Filter network tokens to only show chains with balance

            if (filterZeroBalance) {
                mappedTokens.filter { it.balance > 0 }
            } else {
                mappedTokens
            }
        }
    }
}



val DEFAULT_EXCLUDE_LIST = listOf(
    "http://", "https://", "www.",
    ".com", ".io", ".org", ".net", ".xyz",
    "/", "t.me", "telegram", "twitter", "discord", "t.ly"
)