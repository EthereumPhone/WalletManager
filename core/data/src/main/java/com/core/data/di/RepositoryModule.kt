package com.core.data.di

import com.core.data.repository.AlchemyTokenBalanceRepository
import com.core.data.repository.AlchemyTokenMetadataRepository
import com.core.data.repository.AlchemyTransferRepository
import com.core.data.repository.CoinbaseExchangeRepository
import com.core.data.repository.ExchangeRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.ProtoUserDataRepository
import com.core.data.repository.SendRepository
import com.core.data.repository.SendRepositoryImp
import com.core.data.repository.SwapRepository
import com.core.data.repository.SwapRepositoryImp
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.repository.Web3jNetworkBalanceRepository
import com.core.data.util.ConnectivityManagerNetworkMonitor
import com.core.data.util.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindsNetworkBalanceRepository(
        networkBalanceRepository: Web3jNetworkBalanceRepository
    ): NetworkBalanceRepository

    @Binds
    fun bindsTokenBalanceRepository(
        tokenBalanceRepository: AlchemyTokenBalanceRepository
    ): TokenBalanceRepository

    @Binds
    fun bindsTokenMetadataRepository(
        tokenBalanceRepository: AlchemyTokenMetadataRepository
    ): TokenMetadataRepository

    @Binds
    fun bindsTransferRepository(
        transferRepository: AlchemyTransferRepository
    ): TransferRepository

    @Binds
    @Singleton
    fun bindsProtoUserDataRepository(
        userDataRepository: ProtoUserDataRepository
    ): UserDataRepository

    @Binds
    fun bindsSwapRepository(
        swapRepositoryImp: SwapRepositoryImp
    ): SwapRepository

    @Binds
    @Singleton
    fun bindsSendRepository(
        sendRepositoryImp: SendRepositoryImp
    ): SendRepository

    @Binds
    fun bindsCoinbaseExchangeRepository(
        coinbaseExchangeRepository: CoinbaseExchangeRepository
    ): ExchangeRepository

    @Binds
    fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor

}