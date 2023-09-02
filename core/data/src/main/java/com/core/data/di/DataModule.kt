package com.core.data.di

import android.content.Context
import com.core.data.remote.NetworkBalanceApi
import com.core.data.remote.TokenBalanceApi
import com.core.data.remote.TokenMetadataApi
import com.core.data.remote.TransfersApi
import com.core.data.remote.UniswapApi
import com.core.data.util.MoshiAdapters
import com.core.model.NetworkChain
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.ethosmobile.uniswap_routing_sdk.UniswapRoutingSDK
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
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
            .build()
    }

    @Singleton
    @Provides
    fun provideWeb3j(): Web3j {
        return Web3j.build(
            HttpService(NetworkChain.MAIN.rpc)
        )
    }

    @Singleton
    @Provides
    fun providesWalletSdk(
        @ApplicationContext appContext: Context
    ): WalletSDK {
        return WalletSDK(appContext)
    }

    @Singleton
    @Provides
    fun provideTokenBalanceApi(
        moshi: Moshi
    ): TokenBalanceApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://localhost/")
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
    fun provideTokenMetadataApi(
        moshi: Moshi
    ): TokenMetadataApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://localhost/")
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
            .baseUrl("http://localhost/")
            .build()
            .create(TransfersApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUniswapRouterSDK(@ApplicationContext context: Context): UniswapRoutingSDK {
        return UniswapRoutingSDK(
            context = context,
            web3RPC = NetworkChain.MAIN.rpc
        )
    }


    @Singleton
    @Provides
    fun provideUniSwapApi(
        walletSDK: WalletSDK,
        web3j: Web3j,
        uniswapRoutingSDK: UniswapRoutingSDK
    ): UniswapApi {
        return UniswapApi(
            walletSDK,
            web3j,
            uniswapRoutingSDK
        )
    }

}