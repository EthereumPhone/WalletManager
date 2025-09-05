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

class DefaultGroupedTokenRepository @Inject constructor(
    val tokenGroupDao: TokenGroupDao
): GroupedTokenRepository {
    override fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>> =
        tokenGroupDao.observeAllActiveTokenGroupsWithExchange()
            .map { groups ->
                val filteredGroups = if (!filterList.isNullOrEmpty()) {
                    groups.filter { token -> // Filter out tokens with URLs in their names or symbols
                        val name = token.tokenGroup.name
                        val symbol = token.tokenGroup.symbol

                        val containsNoUrlPatterns = filterList.none { pattern ->
                            name.contains(pattern) || symbol.contains(pattern)
                        }
                        containsNoUrlPatterns
                    }
                } else {
                    groups
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

    override fun observeAllTokensWithPriceInGroup(groupId: String): Flow<List<TokenAssetWithPrice>> =
        tokenGroupDao.observeAllTokensInGroupWithLatestExchange(groupId).map { tokens ->
            tokens.map { it.toExternalModelWithPrice() }
        }
}



val DEFAULT_EXCLUDE_LIST = listOf(
    "http://", "https://", "www.",
    ".com", ".io", ".org", ".net", ".xyz",
    "/", "t.me", "telegram", "twitter", "discord", "t.ly"
)