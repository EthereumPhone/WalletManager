package com.core.domain

import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.model.TokenAssetWithPrice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllTokensWithExchangeUsecase @Inject constructor(
    private val groupedTokenRepository: GroupedTokenRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
){
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<TokenAssetWithPrice>> =
        combine(
            groupedTokenRepository.observeGroupTokensWithExchange(),
            networkBalanceRepository.getNetworkTokensWithExchange()
        ) { groupAssetWithExchanges, networkTokensWithExchange ->
            // Process ERC20 grouped tokens
            val erc20Tokens = groupAssetWithExchanges.flatMap { group ->
                val commonIcon = group.tokens.firstOrNull { !it.logoUrl.isNullOrEmpty() }?.logoUrl

                group.tokens
                    .map { it.copy(logoUrl = commonIcon) }
            }

            // Network tokens already come as individual per-chain tokens with exchange rates
            // Combine both lists
            networkTokensWithExchange + erc20Tokens
        }
}


