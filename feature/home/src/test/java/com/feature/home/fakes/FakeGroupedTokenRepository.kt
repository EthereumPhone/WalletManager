package com.feature.home.fakes

import com.core.data.repository.GroupedTokenRepository
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenGroupAsset
import com.core.model.TokenGroupAssetOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeGroupedTokenRepository(
    initialGroups: List<TokenGroupAssetOverview> = emptyList()
) : GroupedTokenRepository {

    private val erc20GroupsState = MutableStateFlow(initialGroups)
    private val groupedTokensState = MutableStateFlow<List<TokenGroupAsset>>(emptyList())

    override fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>> =
        erc20GroupsState.asStateFlow().map { groups ->
            if (!filterList.isNullOrEmpty()) {
                groups.filter { token ->
                    val name = token.name
                    val symbol = token.symbol
                    filterList.none { pattern ->
                        name.contains(pattern, ignoreCase = true) || symbol.contains(pattern, ignoreCase = true)
                    }
                }
            } else {
                groups
            }
        }

    override fun observeGroupedTokens(): Flow<List<TokenGroupAsset>> =
        groupedTokensState.asStateFlow()

    override fun observeAllTokensWithPriceInGroup(
        groupId: String,
        filterZeroBalance: Boolean
    ): Flow<List<TokenAssetWithPrice>> = MutableStateFlow(emptyList<TokenAssetWithPrice>()).asStateFlow()

    fun emitErc20Groups(groups: List<TokenGroupAssetOverview>) {
        erc20GroupsState.value = groups
    }

    fun emitGroupedTokens(groups: List<TokenGroupAsset>) {
        groupedTokensState.value = groups
    }
}


