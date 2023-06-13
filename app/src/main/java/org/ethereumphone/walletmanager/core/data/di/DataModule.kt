package org.ethereumphone.walletmanager.core.data.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletmanager.core.data.remote.OldExchangeApi
import org.ethereumphone.walletmanager.core.data.remote.TransfersApi
import org.ethereumphone.walletmanager.core.data.repository.NetworkWalletDataRepositoryImplementation
import org.ethereumphone.walletmanager.core.data.repository.TokenExchangeRepositoryImplementation
import org.ethereumphone.walletmanager.core.data.repository.TokenMetadataRepositoryImplementation
import org.ethereumphone.walletmanager.core.database.dao.TokenExchangeDao
import org.ethereumphone.walletmanager.core.database.dao.TokenMetadataDao
import org.ethereumphone.walletmanager.core.domain.repository.TokenExchangeRepository
import org.ethereumphone.walletmanager.core.domain.repository.TokenMetadataRepository
import org.ethereumphone.walletmanager.core.data.util.MoshiAdapters
import org.ethereumphone.walletmanager.core.data.util.TransferAdapter
import org.ethereumphone.walletmanager.core.database.dao.WalletDataDao
import org.ethereumphone.walletmanager.core.domain.repository.NetworkWalletDataRepository
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideWeb3j(): Web3j {
        return Web3j.build(HttpService("https://rpc.ankr.com/eth"))
    }

    @Provides
    @Singleton
    fun provideWalletSdk(
        @ApplicationContext context: Context,
        web3j: Web3j
    ): WalletSDK {
        return WalletSDK(
            context,
            web3j
        )
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(MoshiAdapters())
            .add(TransferAdapter())
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenExchangeApi(
        moshi: Moshi
    ): OldExchangeApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(OldExchangeApi.BASE_URL)
            .build()
            .create(OldExchangeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkTransfersApi(
        moshi: Moshi
    ): TransfersApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(TransfersApi.BASE_URL)
            .build()
            .create(TransfersApi::class.java)
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
        tokenOldExchangeApi: OldExchangeApi
    ): TokenExchangeRepository {
        return TokenExchangeRepositoryImplementation(
            dao = tokenExchangeDao,
            api = tokenOldExchangeApi
        )
    }

    @Provides
    @Singleton
    fun provideWalletService(
        walletSDK: WalletSDK,
        web3j: Web3j,
        walletDataDao: WalletDataDao
    ): NetworkWalletDataRepository {
        return NetworkWalletDataRepositoryImplementation(
            walletSDK,
            web3j,
            walletDataDao
        )
    }
}
