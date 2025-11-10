package com.core.domain

import com.core.data.repository.GroupedTokenRepository
import com.core.model.TokenAssetWithPrice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetAllTokensWithExchangeUsecase @Inject constructor(
    private val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
    private val groupedTokenRepository: GroupedTokenRepository,
){
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<TokenAssetWithPrice>> =
        getAllGroupedTokensUsecase().flatMapLatest { groups ->
            val flows = groups.map { group ->
                groupedTokenRepository.observeAllTokensWithPriceInGroup(group.groupId)
            }
            if (flows.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(flows) { tokenLists ->
                    tokenLists
                        .flatMap { it }
                        .distinctBy { it.address.lowercase() + "_" + it.chainId }
                }
            }
        }
}


