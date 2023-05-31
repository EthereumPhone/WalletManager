package org.ethereumphone.walletmanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumphone.walletmanager.core.util.Resource

interface NetworkWalletDataRepository {
    fun getWalletChainId(): Flow<Resource<Int>>
    fun getWalletAddress(): Flow<Resource<String>>
    fun getNetworkAmount(): Flow<Resource<Double>>
}