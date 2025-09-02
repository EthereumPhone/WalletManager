package com.core.data.repository

import com.core.model.TokenGroupAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.TokenGroupAssetWithPrice
import kotlinx.coroutines.flow.Flow

interface GroupedTokenRepository {

    fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>>
    fun observeGroupedTokens(): Flow<List<TokenGroupAsset>>
    fun observeGroupedTokensWithExchange(): Flow<List<TokenGroupAssetWithPrice>>


}