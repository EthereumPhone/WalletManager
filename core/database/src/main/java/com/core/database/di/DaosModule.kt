package com.core.database.di

import com.core.WmDatabase
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.dao.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DaosModule {
    @Provides
    @Singleton
    fun provideTransferDao(
        database: WmDatabase
    ): TransferDao = database.transferDao

    @Provides
    @Singleton
    fun provideTokenMetadataDao(
        database: WmDatabase
    ): TokenMetadataDao = database.tokenMetadataDao

    @Provides
    @Singleton
    fun provideTokenBalanceDao(
        database: WmDatabase
    ): TokenBalanceDao = database.tokenBalanceDao
}
