package org.ethereumphone.walletmanager.core.data.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletmanager.core.data.remote.ExchangeApi
import org.ethereumphone.walletmanager.core.data.remote.NetworkTransfersApi
import org.ethereumphone.walletmanager.core.data.repository.TokenExchangeRepositoryImplementation
import org.ethereumphone.walletmanager.core.data.repository.TokenMetadataRepositoryImplementation
import org.ethereumphone.walletmanager.core.database.dao.TokenExchangeDao
import org.ethereumphone.walletmanager.core.database.dao.TokenMetadataDao
import org.ethereumphone.walletmanager.core.domain.repository.TokenExchangeRepository
import org.ethereumphone.walletmanager.core.domain.repository.TokenMetadataRepository
import org.ethereumphone.walletmanager.core.data.util.ExchangeAdapter
import org.ethereumphone.walletmanager.core.data.util.TransferAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(ExchangeAdapter())
            .add(TransferAdapter())
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenExchangeApi(
        moshi: Moshi
    ): ExchangeApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(ExchangeApi.BASE_URL)
            .build()
            .create(ExchangeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkTransfersApi(
        moshi: Moshi
    ): NetworkTransfersApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(NetworkTransfersApi.BASE_URL)
            .build()
            .create(NetworkTransfersApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenMetadataRepository(tokenMetadataDao: TokenMetadataDao): TokenMetadataRepository {
        return TokenMetadataRepositoryImplementation(tokenMetadataDao)
    }

    @Provides
    @Singleton
    fun provideTokenExchangeRepository(
        tokenExchangeDao: TokenExchangeDao,
        tokenExchangeApi: ExchangeApi
    ): TokenExchangeRepository {
        return TokenExchangeRepositoryImplementation(
            dao = tokenExchangeDao,
            api = tokenExchangeApi
        )
    }
}
