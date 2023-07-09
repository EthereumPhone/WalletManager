package com.core.data.di

import com.core.data.repository.AlchemyTokenBalanceRepository
import com.core.data.repository.AlchemyTokenMetadataRepository
import com.core.data.repository.AlchemyTransferRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.ProtoUserDataRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.repository.Web3jNetworkBalanceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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
    fun bindsProtoUserDataRepository(
        userDataRepository: ProtoUserDataRepository
    ): UserDataRepository

}