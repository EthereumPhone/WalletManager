package com.core.data.di

import android.content.Context
import android.os.Build
import androidx.tracing.trace
import com.core.data.BuildConfig
import com.core.data.remote.ClaimDataApi
import com.core.data.remote.EnsApi
import com.core.data.remote.Erc20TransferApi
import com.core.data.remote.NetworkBalanceApi
import com.core.data.remote.TokenBalanceApi
import com.core.data.remote.TokenMetadataApi
import com.core.data.remote.TransfersApi
import com.core.data.remote.UniswapApi
import com.core.data.remote.DexScreenerApiClient
import com.core.data.remote.DexScreenerDataSource
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainToApiKey
import com.core.datastore.ExclusionListProtoSerializer
import com.core.model.NetworkChain
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalSDK
import com.core.terminalsdk.TerminalSDKWrapper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bouncycastle.util.Fingerprint
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
    ): WalletSDK? {
        return if (isEmulator) {
            null
        } else {
            WalletSDK(appContext, bundlerRPCUrl = chainIdToBundler(1))
        }
    }

    @Provides
    @Singleton
    fun provideTerminalSDKWrapper(@ApplicationContext context: Context): TerminalSDKWrapper {
        return try {
            TerminalSDKWrapper.Available(TerminalSDK(context))
        } catch (e: Exception) {
            TerminalSDKWrapper.Unavailable
        }
    }

    @Provides
    @Singleton
    fun provideTerminalSDK(terminalSDKWrapper: TerminalSDKWrapper): TerminalSDK? {
        return when (terminalSDKWrapper) {
            is TerminalSDKWrapper.Available -> terminalSDKWrapper.sdk
            TerminalSDKWrapper.Unavailable -> null
        }
    }

    @Singleton
    @Provides
    fun provideReflectiveLedPattern(): ReflectiveLedPattern? {
        return try {
            ReflectiveLedPattern()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
    fun provideClaimDataApi(
        moshi: Moshi
    ): ClaimDataApi {
        val client: OkHttpClient = OkHttpClient.Builder()
            .build()
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("https://api.markushaas.com/")
            .client(client)
            .build()
            .create(ClaimDataApi::class.java)
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
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
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
        walletSDK: WalletSDK?,
        web3j: Web3j,
        uniswapRoutingSDK: UniswapRoutingSDK
    ): UniswapApi? {
        return if (walletSDK == null) {
            null
        } else {
            UniswapApi(
                walletSDK,
                web3j,
                uniswapRoutingSDK,
                context
            )
        }
    }

    @Singleton
    @Provides
    fun provideErc20TransferApi(
        @ApplicationContext context: Context
    ): Erc20TransferApi {
        return Erc20TransferApi(context)
    }

    @Singleton
    @Provides
    fun provideEnsApi(): EnsApi {
        return EnsApi()
    }

    @Singleton
    @Provides
    fun provideDexScreenerDataSource(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): DexScreenerDataSource {
        return DexScreenerApiClient(okHttpClient, moshi)
    }


    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory = trace("NiaOkHttpClient") {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            setLevel(HttpLoggingInterceptor.Level.BODY)
                        }
                    },
            )
            .build()
    }


    private val isEmulator: Boolean
        get() = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")


}