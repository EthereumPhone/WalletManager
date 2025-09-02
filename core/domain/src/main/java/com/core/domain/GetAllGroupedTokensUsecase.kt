package com.core.domain

import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.model.TokenAsset
import com.core.model.TokenGroupAssetOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAllGroupedTokensUsecase @Inject constructor(
    private val groupedTokenRepository: GroupedTokenRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
) {

    operator fun invoke(filterList: List<String>? = emptyList()): Flow<List<TokenGroupAssetOverview>> =
        combine(
            groupedTokenRepository.observeGroupedTokensOverview(filterList),
            networkBalanceRepository.getGroupedNetworkTokensOverview()
        ) { erc20, network ->
            (erc20 + network)
        }

}