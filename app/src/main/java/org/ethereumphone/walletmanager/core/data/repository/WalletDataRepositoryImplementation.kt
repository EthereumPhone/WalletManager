package org.ethereumphone.walletmanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumphone.walletmanager.core.domain.repository.NetworkWalletDataRepository
import org.ethereumphone.walletmanager.core.util.Resource
import javax.inject.Inject

class NetworkWalletDataRepositoryImplementation @Inject constructor(


): NetworkWalletDataRepository {
    override fun getWalletChainId(): Flow<Resource<Int>> {

    }

    override fun getWalletAddress(): Flow<Resource<String>> {
        TODO("Not yet implemented")
    }

    override fun getNetworkAmount(): Flow<Resource<Double>> {
        TODO("Not yet implemented")
    }

}