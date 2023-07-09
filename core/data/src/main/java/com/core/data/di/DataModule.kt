package com.core.data.di

import android.content.Context
import com.core.data.remote.NetworkBalanceApi
import com.core.data.remote.TokenBalanceApi
import com.core.data.remote.TokenMetadataApi
import com.core.data.remote.TransfersApi
import com.core.data.util.MoshiAdapters
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletsdk.WalletSDK
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .add(MoshiAdapters())
            .build()
    }

    @Singleton
    @Provides
    fun provideTokenBalanceApi(
        moshi: Moshi
    ): TokenBalanceApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(TokenBalanceApi.BASE_URL)
            .build()
            .create(TokenBalanceApi::class.java)
    }

    @Singleton
    @Provides
    fun provideNetworkBalanceApi(): NetworkBalanceApi {
        return NetworkBalanceApi()
    }

    @Singleton
    @Provides
    fun provideTokenMetadataApi(): TokenMetadataApi {
        return Retrofit.Builder()
            .baseUrl(TokenBalanceApi.BASE_URL)
            .build()
            .create(TokenMetadataApi::class.java)
    }

    @Singleton
    @Provides
    fun provideTransferApi(
        moshi: Moshi
    ): TransfersApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(TransfersApi.BASE_URL)
            .build()
            .create(TransfersApi::class.java)
    }

    @Singleton
    @Provides
    fun providesWalletSdk(
        @ApplicationContext appContext: Context
    ): WalletSDK {
        return WalletSDK(appContext)
    }

}