package com.feature.home.fakes

import com.core.domain.GetAllGroupedTokensUsecase
import com.core.model.TokenGroupAssetOverview

object HomeUsecaseFactory {
    data class GetAllGroupedTokensComponents(
        val usecase: GetAllGroupedTokensUsecase,
        val groupedTokenRepository: FakeGroupedTokenRepository,
        val networkBalanceRepository: FakeNetworkBalanceRepository
    )

    fun createGetAllGroupedTokensUsecase(
        erc20Groups: List<TokenGroupAssetOverview> = emptyList(),
        networkGroups: List<TokenGroupAssetOverview> = emptyList()
    ): GetAllGroupedTokensComponents {
        val groupedRepo = FakeGroupedTokenRepository(initialGroups = erc20Groups)
        val networkRepo = FakeNetworkBalanceRepository(initialNetworkGroups = networkGroups)
        val usecase = GetAllGroupedTokensUsecase(
            groupedTokenRepository = groupedRepo,
            networkBalanceRepository = networkRepo
        )
        return GetAllGroupedTokensComponents(
            usecase = usecase,
            groupedTokenRepository = groupedRepo,
            networkBalanceRepository = networkRepo
        )
    }
}


