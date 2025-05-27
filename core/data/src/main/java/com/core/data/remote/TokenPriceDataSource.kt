package com.core.data.remote

import com.core.data.model.dto.NetworkTokenExchange

interface TokenPriceDataSource {
    suspend fun fetchTokenPriceByAddresses(
        networks: List<String>,
        addresses: List<String>
    ): List<NetworkTokenExchange>
    suspend fun fetchTokenPriceBySymbols(symbols: List<String>): List<NetworkTokenExchange>
}