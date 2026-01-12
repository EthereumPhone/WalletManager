package com.feature.swap.fakes

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

    private val groupsOverviewState = MutableStateFlow(initialGroups)
    private val groupedTokensState = MutableStateFlow<List<TokenGroupAsset>>(emptyList())
    private val tokensByGroupState = MutableStateFlow<Map<String, List<TokenAssetWithPrice>>>(emptyMap())

    override fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>> =
        groupsOverviewState.asStateFlow().map { groups ->
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
    ): Flow<List<TokenAssetWithPrice>> =
        tokensByGroupState.asStateFlow().map { map ->
            val tokens = map[groupId].orEmpty()
            if (filterZeroBalance) tokens.filter { it.balance > 0.0 } else tokens
        }

    fun emitGroupsOverview(groups: List<TokenGroupAssetOverview>) {
        groupsOverviewState.value = groups
    }

    fun emitGroupedTokens(groups: List<TokenGroupAsset>) {
        groupedTokensState.value = groups
    }

    fun emitTokensForGroup(groupId: String, tokens: List<TokenAssetWithPrice>) {
        val current = tokensByGroupState.value.toMutableMap()
        current[groupId] = tokens
        tokensByGroupState.value = current.toMap()
    }
}



