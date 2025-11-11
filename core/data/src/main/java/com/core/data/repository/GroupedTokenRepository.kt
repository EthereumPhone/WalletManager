package com.core.data.repository

import com.core.model.TokenAssetWithPrice
import com.core.model.TokenGroupAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.TokenGroupAssetWithExchange
import kotlinx.coroutines.flow.Flow

interface GroupedTokenRepository {

    fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>>
    fun observeGroupedTokens(): Flow<List<TokenGroupAsset>>

    fun observeGroupTokensWithExchange(): Flow<List<TokenGroupAssetWithExchange>>

    fun observeAllTokensWithPriceInGroup(groupId: String, filterZeroBalance: Boolean = false): Flow<List<TokenAssetWithPrice>>


}