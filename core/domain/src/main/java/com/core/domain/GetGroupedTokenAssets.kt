package com.core.domain

import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGroupedTokenAssets @Inject constructor(
    private val getTokenBalancesWithMetadataUseCase: GetTokenBalancesWithMetadataUseCase
) {

    operator fun invoke(): Flow<Map<String, List<TokenAsset>>> =
        getTokenBalancesWithMetadataUseCase().map { assets ->
            assets.groupBy { it.symbol }
    }
}