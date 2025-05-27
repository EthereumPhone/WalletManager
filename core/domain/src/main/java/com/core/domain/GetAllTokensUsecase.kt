package com.core.domain

import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.formatSmallBalance
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAllTokensUsecase @Inject constructor(
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
) {
    operator fun invoke(): Flow<List<TokenAsset>> =
        combine(
            tokenBalanceRepository.getTokens(),
            networkBalanceRepository.getNetworkTokens()
        ) { erc20, network ->


            if (network.any { it.symbol == "DEGEN" }) {
                println("test now")
            }

            (erc20 + network)
            /*
            (network + erc20).map {
                it.copy(
                    balance = formatSmallBalance(it.balance)
                )
            }
             */

        }
}