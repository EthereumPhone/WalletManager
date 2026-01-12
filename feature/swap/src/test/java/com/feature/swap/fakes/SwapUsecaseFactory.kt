package com.feature.swap.fakes

import com.core.domain.GetAllGroupedTokensUsecase
import com.core.domain.GetAllTokensUsecase
import com.core.domain.GetAllTokensWithExchangeUsecase
import com.core.domain.GetSwapTokens
import com.core.domain.GetSwappableTokensForSelection
import com.core.domain.QueryTokenAssetsByNetwork
import com.core.model.TokenGroupAssetOverview

object SwapUsecaseFactory {

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

    data class GetAllTokensWithExchangeComponents(
        val usecase: GetAllTokensWithExchangeUsecase,
        val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
        val groupedTokenRepository: FakeGroupedTokenRepository,
        val networkBalanceRepository: FakeNetworkBalanceRepository
    )

    fun createGetAllTokensWithExchangeUsecase(
        erc20Groups: List<TokenGroupAssetOverview> = emptyList(),
        networkGroups: List<TokenGroupAssetOverview> = emptyList()
    ): GetAllTokensWithExchangeComponents {
        val groupedTokens = createGetAllGroupedTokensUsecase(erc20Groups, networkGroups)
        val usecase = GetAllTokensWithExchangeUsecase(
            getAllGroupedTokensUsecase = groupedTokens.usecase,
            groupedTokenRepository = groupedTokens.groupedTokenRepository
        )
        return GetAllTokensWithExchangeComponents(
            usecase = usecase,
            getAllGroupedTokensUsecase = groupedTokens.usecase,
            groupedTokenRepository = groupedTokens.groupedTokenRepository,
            networkBalanceRepository = groupedTokens.networkBalanceRepository
        )
    }

    data class GetAllTokensComponents(
        val usecase: GetAllTokensUsecase,
        val tokenBalanceRepository: FakeTokenBalanceRepository,
        val networkBalanceRepository: FakeNetworkBalanceRepository
    )

    fun createGetAllTokensUsecase(): GetAllTokensComponents {
        val tokenBalanceRepository = FakeTokenBalanceRepository()
        val networkBalanceRepository = FakeNetworkBalanceRepository()
        val usecase = GetAllTokensUsecase(
            tokenBalanceRepository = tokenBalanceRepository,
            networkBalanceRepository = networkBalanceRepository
        )
        return GetAllTokensComponents(
            usecase = usecase,
            tokenBalanceRepository = tokenBalanceRepository,
            networkBalanceRepository = networkBalanceRepository
        )
    }

    data class GetSwapTokensComponents(
        val usecase: GetSwapTokens,
        val tokenMetadataRepository: FakeTokenMetadataRepository,
        val networkBalanceRepository: FakeNetworkBalanceRepository,
        val tokenBalanceRepository: FakeTokenBalanceRepository
    )

    fun createGetSwapTokensUsecase(): GetSwapTokensComponents {
        val tokenMetadataRepository = FakeTokenMetadataRepository()
        val networkBalanceRepository = FakeNetworkBalanceRepository()
        val tokenBalanceRepository = FakeTokenBalanceRepository()
        val usecase = GetSwapTokens(
            tokenMetadataRepository = tokenMetadataRepository,
            networkBalanceRepository = networkBalanceRepository,
            tokenBalanceRepository = tokenBalanceRepository
        )
        return GetSwapTokensComponents(
            usecase = usecase,
            tokenMetadataRepository = tokenMetadataRepository,
            networkBalanceRepository = networkBalanceRepository,
            tokenBalanceRepository = tokenBalanceRepository
        )
    }

    data class QueryTokenAssetsByNetworkComponents(
        val usecase: QueryTokenAssetsByNetwork,
        val tokenMetadataRepository: FakeTokenMetadataRepository,
        val tokenBalanceRepository: FakeTokenBalanceRepository
    )

    fun createQueryTokenAssetsByNetworkUsecase(): QueryTokenAssetsByNetworkComponents {
        val tokenMetadataRepository = FakeTokenMetadataRepository()
        val tokenBalanceRepository = FakeTokenBalanceRepository()
        val usecase = QueryTokenAssetsByNetwork(
            tokenMetadataRepository = tokenMetadataRepository,
            tokenBalanceRepository = tokenBalanceRepository
        )
        return QueryTokenAssetsByNetworkComponents(
            usecase = usecase,
            tokenMetadataRepository = tokenMetadataRepository,
            tokenBalanceRepository = tokenBalanceRepository
        )
    }

    data class GetSwappableTokensForSelectionComponents(
        val usecase: GetSwappableTokensForSelection,
        val groupedTokenRepository: FakeGroupedTokenRepository,
        val userDataRepository: FakeUserDataRepository,
        val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
        val getSwapTokens: GetSwapTokens,
        val networkBalanceRepository: FakeNetworkBalanceRepository,
        val tokenBalanceRepository: FakeTokenBalanceRepository,
        val tokenMetadataRepository: FakeTokenMetadataRepository
    )

    fun createGetSwappableTokensForSelectionUsecase(
        userData: com.core.model.UserData = com.core.model.UserData(
            walletAddress = "",
            walletNetwork = "8453",
            isFirstBoot = false,
            preferredCurrency = "usd"
        ),
        erc20Groups: List<TokenGroupAssetOverview> = emptyList(),
        networkGroups: List<TokenGroupAssetOverview> = emptyList()
    ): GetSwappableTokensForSelectionComponents {
        val groupedRepo = FakeGroupedTokenRepository(initialGroups = erc20Groups)
        val networkRepo = FakeNetworkBalanceRepository(initialNetworkGroups = networkGroups)
        val getAllGroupedTokensUsecase = GetAllGroupedTokensUsecase(
            groupedTokenRepository = groupedRepo,
            networkBalanceRepository = networkRepo
        )

        val tokenMetadataRepository = FakeTokenMetadataRepository()
        val tokenBalanceRepository = FakeTokenBalanceRepository()
        val getSwapTokens = GetSwapTokens(
            tokenMetadataRepository = tokenMetadataRepository,
            networkBalanceRepository = networkRepo,
            tokenBalanceRepository = tokenBalanceRepository
        )

        val userDataRepository = FakeUserDataRepository(initialUserData = userData)

        val usecase = GetSwappableTokensForSelection(
            getAllGroupedTokensUsecase = getAllGroupedTokensUsecase,
            getSwapTokens = getSwapTokens,
            groupedTokenRepository = groupedRepo,
            userDataRepository = userDataRepository
        )

        return GetSwappableTokensForSelectionComponents(
            usecase = usecase,
            groupedTokenRepository = groupedRepo,
            userDataRepository = userDataRepository,
            getAllGroupedTokensUsecase = getAllGroupedTokensUsecase,
            getSwapTokens = getSwapTokens,
            networkBalanceRepository = networkRepo,
            tokenBalanceRepository = tokenBalanceRepository,
            tokenMetadataRepository = tokenMetadataRepository
        )
    }
}



