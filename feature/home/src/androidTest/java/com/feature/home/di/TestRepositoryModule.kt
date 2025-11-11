package com.feature.home.di

import com.core.data.di.RepositoryModule
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.NetworkMonitor
import com.core.model.TokenGroupAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.UserData
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
interface TestRepositoryModule {

    @Binds
    @Singleton
    fun bindUserDataRepository(impl: FakeUserDataRepository): UserDataRepository

    @Binds
    @Singleton
    fun bindTransferRepository(impl: FakeTransferRepository): TransferRepository

    @Binds
    @Singleton
    fun bindNetworkMonitor(impl: FakeNetworkMonitor): NetworkMonitor

    @Binds
    @Singleton
    fun bindGroupedTokenRepository(impl: FakeGroupedTokenRepository): GroupedTokenRepository

    @Binds
    @Singleton
    fun bindNetworkBalanceRepository(impl: FakeNetworkBalanceRepository): NetworkBalanceRepository
}

@Singleton
class FakeUserDataRepository @Inject constructor() : UserDataRepository {
    override val userData: Flow<UserData> = flowOf(
        UserData(
            walletAddress = "0x0000000000000000000000000000000000000000",
            walletNetwork = "mainnet",
            isFirstBoot = false,
            preferredCurrency = "usd"
        )
    )
    override suspend fun setWalletAddress(address: String) {}
    override suspend fun setWalletNetwork(network: String) {}
    override suspend fun setIsFirstBoot(isFirstBoot: Boolean) {}
    override suspend fun setPreferredCurrency(currency: String) {}
}

@Singleton
class FakeTransferRepository @Inject constructor() : TransferRepository {
    override fun getTransfers() = flowOf(emptyList<com.core.model.Transfer>())
    override fun getTransfers(chainId: Int) = flowOf(emptyList<com.core.model.Transfer>())
    override fun getTransfers(categories: List<String>) = flowOf(emptyList<com.core.model.Transfer>())
    override fun getTransfers(chainId: Int, categories: List<String>) = flowOf(emptyList<com.core.model.Transfer>())
    override fun observeTransfersExist(): Flow<Boolean> = flowOf(false)
    override suspend fun refreshTransfers(address: String) {}
}

@Singleton
class FakeNetworkMonitor @Inject constructor() : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flowOf(true)
}

@Singleton
class FakeGroupedTokenRepository @Inject constructor() : GroupedTokenRepository {
    override fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>> =
        flowOf(
            listOf(
                TokenGroupAssetOverview(
                    groupId = "erc20_usdc",
                    symbol = "USDC",
                    name = "USD Coin",
                    logoUrl = null,
                    totalBalance = 0.0,
                    formattedBalance = "0",
                    totalFiatBalance = 0.0,
                    formattedFiatBalance = "0.00",
                    exchangeCurrency = "usd"
                )
            )
        )

    override fun observeGroupedTokens(): Flow<List<TokenGroupAsset>> = flowOf(emptyList())

    override fun observeAllTokensWithPriceInGroup(
        groupId: String,
        filterZeroBalance: Boolean
    ): Flow<List<com.core.model.TokenAssetWithPrice>> = flowOf(emptyList())
}

@Singleton
class FakeNetworkBalanceRepository @Inject constructor() : NetworkBalanceRepository {
    override fun getNetworkTokens(): Flow<List<com.core.model.TokenAsset>> = flowOf(emptyList())
    override fun getGroupedNetworkTokens(): Flow<List<com.core.model.TokenAsset>> = flowOf(emptyList())
    override fun getGroupedNetworkTokensOverview(): Flow<List<TokenGroupAssetOverview>> = flowOf(emptyList())
    override fun getNetworkBalance(chainId: Int): Flow<com.core.model.TokenBalance> =
        flowOf(
            com.core.model.TokenBalance(
                contractAddress = "network_native",
                chainId = chainId,
                tokenBalance = java.math.BigDecimal.ZERO
            )
        )
    override suspend fun refreshNetworkBalance(toAddress: String, chainIds: List<Int>) {}
    override suspend fun refreshNetworkBalanceByNetwork(toAddress: String, chainId: Int) {}
}


