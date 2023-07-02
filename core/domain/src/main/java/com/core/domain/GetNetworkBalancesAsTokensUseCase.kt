package com.core.domain

import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository

/**
 * Users can have the same erc20 token on multiple chains.
 * The purpose of this use case, is to group the balance of such tokens across multiple chains
 */
class GetNetworkBalancesAsTokensUseCase(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository
) {

}