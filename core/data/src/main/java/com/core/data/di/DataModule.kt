package com.core.data.di

import android.content.Context
import com.core.data.remote.CoinbaseTokenExchangeApi
import com.core.data.remote.EnsApi
import com.core.data.remote.Erc20TransferApi
import com.core.data.remote.NetworkBalanceApi
import com.core.data.remote.TokenBalanceApi
import com.core.data.remote.TokenMetadataApi
import com.core.data.remote.TransfersApi
import com.core.data.remote.UniswapApi
import com.core.data.util.chainToApiKey
import com.core.model.NetworkChain
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.ethereumphone.walletsdk.WalletSDK
import org.ethosmobile.uniswap_routing_sdk.UniswapRoutingSDK
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
            HttpService("https://${NetworkChain.MAINNET.chainName}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.MAINNET.chainName)}")
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
        val logging = HttpLoggingInterceptor()
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            //.addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://localhost/")
            .client(client)
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
        val logging = HttpLoggingInterceptor()
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            //.addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://localhost/")
            .client(client)
            .build()
            .create(TokenMetadataApi::class.java)
    }

    @Singleton
    @Provides
    fun provideTransferApi(
        moshi: Moshi
    ): TransfersApi {
        val logging = HttpLoggingInterceptor()
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            //.addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://localhost/")
            .client(client)
            .build()
            .create(TransfersApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCoinbaseTokenExchangeApi(
        moshi: Moshi
    ): CoinbaseTokenExchangeApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("https://api.coinbase.com/")
            .build()
            .create(CoinbaseTokenExchangeApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUniswapRouterSDK(@ApplicationContext context: Context): UniswapRoutingSDK {
        return UniswapRoutingSDK(
            context = context,
            web3RPC = "https://${NetworkChain.MAINNET.chainName}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.MAINNET.chainName)}"
        )
    }

    @Singleton
    @Provides
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }


    @Singleton
    @Provides
    fun provideUniSwapApi(
        @ApplicationContext context: Context,
        walletSDK: WalletSDK,
        web3j: Web3j,
        uniswapRoutingSDK: UniswapRoutingSDK
    ): UniswapApi {
        return UniswapApi(
            walletSDK,
            web3j,
            uniswapRoutingSDK,
            context
        )
    }

    @Singleton
    @Provides
    fun provideErc20TransferApi(
        walletSDK: WalletSDK,
        web3j: Web3j
    ): Erc20TransferApi {
        return Erc20TransferApi(
            walletSDK,
            web3j
        )
    }

    @Singleton
    @Provides
    fun provideEnsApi(): EnsApi {
        return EnsApi()
    }

}