package com.core.data.di

import androidx.tracing.trace
import com.core.data.BuildConfig
import com.core.data.remote.RetrofitTokenPrice
import com.core.data.remote.TokenPriceDataSource
import com.core.data.repository.TokenExchangeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RetrofitModule {

    @Binds
    fun bindNetworkTokenPrice(impl: RetrofitTokenPrice): TokenPriceDataSource






}