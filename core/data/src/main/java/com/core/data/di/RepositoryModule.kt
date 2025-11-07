package com.core.data.di

import com.core.data.repository.AlchemyTokenBalanceRepository
import com.core.data.exchange.ExchangeRepository
import com.core.data.exchange.ExchangeRepositoryImpl
import com.core.data.repository.AlchemyTokenMetadataRepository
import com.core.data.repository.AlchemyTransferRepository
import com.core.data.repository.DefaultExchangeRepository
import com.core.data.repository.DefaultGroupedTokenRepository
import com.core.data.repository.EnsRepository
import com.core.data.repository.EnsRepositoryImpl
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.TokenExchangeRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.ProtoUserDataRepository
import com.core.data.repository.SendRepository
import com.core.data.repository.SendRepositoryImp
import com.core.data.repository.SwapRepository
import com.core.data.repository.SwapRepositoryImp
import com.core.data.repository.TerminalRepository
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
    fun bindsTokenExchangeRepository(
        tokenExchangeRepository: DefaultExchangeRepository
    ): TokenExchangeRepository

    @Binds
    fun bindsSendRepository(
        sendRepository: SendRepositoryImp
    ): SendRepository

    @Binds
    fun bindsSwapRepository(
        swapRepository: SwapRepositoryImp
    ): SwapRepository

    @Binds
    fun bindsExchangeRepository(
        impl: ExchangeRepositoryImpl
    ): ExchangeRepository

    @Binds
    fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor
    ): NetworkMonitor
    
    @Binds
    fun bindsEnsRepository(
        ensRepository: EnsRepositoryImpl
    ): EnsRepository

    @Binds
    fun bindsGroupedTokenRepository(
        groupedTokenRepository: DefaultGroupedTokenRepository
    ): GroupedTokenRepository
}