package com.core.database.di

import com.core.database.WmDatabase
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenExchangeDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.dao.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    fun provideTransferDao(
        database: WmDatabase
    ): TransferDao = database.transferDao

    @Provides
    fun provideTokenMetadataDao(
        database: WmDatabase
    ): TokenMetadataDao = database.tokenMetadataDao

    @Provides
    fun provideTokenBalanceDao(
        database: WmDatabase
    ): TokenBalanceDao = database.tokenBalanceDao

    @Provides
    fun provideTokenExchangeDao(
        database: WmDatabase
    ): TokenExchangeDao = database.tokenExchangeDao
}
