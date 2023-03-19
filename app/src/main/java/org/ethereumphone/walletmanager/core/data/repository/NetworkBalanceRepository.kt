package org.ethereumphone.walletmanager.core.data.repository

import kotlinx.coroutines.flow.Flow

interface NetworkBalanceRepository {
    /**
     * Returns the network's token balance
     */
    suspend fun getNetworkBalance(): Flow<Float>

    /**
     * Returns the exchange of the network balance as fiat currency
     */
    suspend fun getNetworkBalanceExchange(): Flow<Float>
}