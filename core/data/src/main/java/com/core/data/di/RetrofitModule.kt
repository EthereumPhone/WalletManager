package com.core.data.di

import com.core.data.remote.RetrofitTokenPrice
import com.core.data.remote.TokenPriceDataSource
import com.core.data.repository.TokenExchangeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RetrofitModule {

    @Binds
    fun bindNetworkTokenPrice(impl: RetrofitTokenPrice): TokenPriceDataSource
}