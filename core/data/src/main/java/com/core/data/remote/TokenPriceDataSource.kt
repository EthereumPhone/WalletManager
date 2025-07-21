package com.core.data.remote

import com.core.data.model.dto.NetworkTokenExchange
import com.core.data.model.dto.TokenAddress
import com.core.data.model.dto.TokenPricesResponse

interface TokenPriceDataSource {
    suspend fun fetchTokenPriceBySymbols(symbols: List<String>): List<NetworkTokenExchange>
    suspend fun fetchTokenPriceByAddresses(addresses: List<TokenAddress>): TokenPricesResponse
}