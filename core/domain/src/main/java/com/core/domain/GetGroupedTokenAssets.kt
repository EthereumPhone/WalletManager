package com.core.domain

import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGroupedTokenAssets @Inject constructor(
    private val getAllTokensUsecase: GetAllTokensUsecase
) {

    operator fun invoke(): Flow<Map<String, List<TokenAsset>>> =
        getAllTokensUsecase().map { assets ->
            assets.groupBy { it.symbol }
    }
}