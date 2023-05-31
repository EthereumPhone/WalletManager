package org.ethereumphone.walletmanager.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletmanager.core.database.WmDatabase
import org.ethereumphone.walletmanager.core.database.dao.TokenExchangeDao
import org.ethereumphone.walletmanager.core.database.dao.TokenMetadataDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    @Singleton
    fun provideExchangeDao(
        database: WmDatabase
    ): TokenExchangeDao = database.tokenExchangeDao()

    @Provides
    @Singleton
    fun provideMetadataDao(
        database: WmDatabase
    ): TokenMetadataDao = database.tokenMetadataDao()
}