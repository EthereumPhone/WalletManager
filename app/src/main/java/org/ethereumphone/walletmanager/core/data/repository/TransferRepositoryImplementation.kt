package org.ethereumphone.walletmanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ethereumphone.walletmanager.BuildConfig
import org.ethereumphone.walletmanager.core.data.remote.TransfersApi
import org.ethereumphone.walletmanager.core.data.remote.dto.request.NetworkTransferRequestBody
import org.ethereumphone.walletmanager.core.database.dao.TransferDao
import org.ethereumphone.walletmanager.core.database.dao.WalletDataDao
import org.ethereumphone.walletmanager.core.database.model.EntryCategory
import org.ethereumphone.walletmanager.core.domain.model.Transfer
import org.ethereumphone.walletmanager.core.domain.repository.NetworkTransferDataRepository
import org.ethereumphone.walletmanager.core.util.Resource
import javax.inject.Inject

/**
 * Not super clean, but I don't want to waste more time just to remove code repetition
 */
class TransferRepositoryImplementation @Inject constructor(
    private val walletDataDao: WalletDataDao,
    private val transfers: TransfersApi,
    private val dao: TransferDao
): NetworkTransferDataRepository {
    override fun getAllNetworksTransfers(): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading())
        val networksTransfers = dao.getAllTransfers().map { it.toTransfer() }
        emit(Resource.Loading(networksTransfers))

        val networks = walletDataDao.getAllNetworksData().filter {
            it.name.isNotBlank() && chainToApiKey(it.name).isNotBlank() }

        networks.forEach { networkWalletData ->
            runCatching {
                val apiKey = chainToApiKey(networkWalletData.name)
                val results = transfers.getTransfers(
                    network = networkWalletData.name,
                    apiKey = apiKey,
                    requestBody = NetworkTransferRequestBody(
                        params = listOf(
                            NetworkTransferRequestBody.NetworkTransferRequestParams(toAddress = networkWalletData.address)
                        )
                    )
                ).map {it.toTransferEntity(networkWalletData.chainId)}
                dao.insertTransfers(results)
            }.onFailure { e ->
                e.printStackTrace()
                emit(Resource.Error(
                    message ="Couldn't complete request",
                    data = networksTransfers
                ))
            }.getOrNull()
        }
        val newNetworksTransfers = dao.getAllTransfers().map { it.toTransfer() }
        emit(Resource.Success(newNetworksTransfers))
    }

    override fun getAllTransfersByChainId(chainId: Int): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading())
        val networksTransfers = dao.getAllTransfersByChain(chainId).map { it.toTransfer() }
        emit(Resource.Loading(networksTransfers))

        val network = walletDataDao.getNetworkDataByChainId(chainId)

        if(network.name.isNotBlank() && chainToApiKey(network.name).isNotBlank()) {
            runCatching {
                val apiKey = chainToApiKey(network.name)
                val results = transfers.getTransfers(
                    network = network.name,
                    apiKey = apiKey,
                    requestBody = NetworkTransferRequestBody(
                        params = listOf(
                            NetworkTransferRequestBody.NetworkTransferRequestParams(toAddress = network.address)
                        )
                    )
                ).map { it.toTransferEntity(network.chainId)}
                dao.insertTransfers(results)

            }.onFailure { e ->
                e.printStackTrace()
                emit(Resource.Error(
                    message ="Couldn't complete request",
                    data = networksTransfers
                ))
            }.getOrNull()
        }
        val newNetworksTransfers = dao.getAllTransfersByChain(chainId).map { it.toTransfer() }
        emit(Resource.Success(newNetworksTransfers))
    }

    override fun getAllTransfersByCategories(categories: List<EntryCategory>): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading())
        val networksTransfers = dao.getAllTransfersByCategories(categories).map { it.toTransfer() }
        emit(Resource.Loading(networksTransfers))

        val networks = walletDataDao.getAllNetworksData().filter {
            it.name.isNotBlank() && chainToApiKey(it.name).isNotBlank() }

        networks.forEach { networkWalletData ->
            runCatching {
                val apiKey = chainToApiKey(networkWalletData.name)
                val results = transfers.getTransfers(
                    network = networkWalletData.name,
                    apiKey = apiKey,
                    requestBody = NetworkTransferRequestBody(
                        params = listOf(
                            NetworkTransferRequestBody.NetworkTransferRequestParams(
                                toAddress = networkWalletData.address,
                                category = categories.map { it.query }
                            )
                        )
                    )
                ).map {it.toTransferEntity(networkWalletData.chainId)}
                dao.insertTransfers(results)
            }.onFailure { e ->
                e.printStackTrace()
                emit(Resource.Error(
                    message ="Couldn't complete request",
                    data = networksTransfers
                ))
            }.getOrNull()
        }
        val newNetworksTransfers = dao.getAllTransfersByCategories(categories).map { it.toTransfer() }
        emit(Resource.Success(newNetworksTransfers))
    }

    override fun getAllTransfersByChainAndCategories(
        chainId: Int,
        categories: List<EntryCategory>
    ): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading())
        val networksTransfers = dao.getAllTransfersByChainAndCategories(chainId, categories).map { it.toTransfer() }
        emit(Resource.Loading(networksTransfers))

        val network = walletDataDao.getNetworkDataByChainId(chainId)

        if(network.name.isNotBlank() && chainToApiKey(network.name).isNotBlank()) {
            runCatching {
                val apiKey = chainToApiKey(network.name)
                val results = transfers.getTransfers(
                    network = network.name,
                    apiKey = apiKey,
                    requestBody = NetworkTransferRequestBody(
                        params = listOf(
                            NetworkTransferRequestBody.NetworkTransferRequestParams(
                                toAddress = network.address,
                                category = categories.map { it.query }
                            )
                        )
                    )
                ).map { it.toTransferEntity(network.chainId)}
                dao.insertTransfers(results)

            }.onFailure { e ->
                e.printStackTrace()
                emit(Resource.Error(
                    message ="Couldn't complete request",
                    data = networksTransfers
                ))
            }.getOrNull()
        }
        val newNetworksTransfers = dao.getAllTransfersByChainAndCategories(chainId, categories).map { it.toTransfer() }
        emit(Resource.Success(newNetworksTransfers))
    }

    private fun chainToApiKey(networkName: String): String = when(networkName) {
        "eth-mainnet" -> BuildConfig.ETHEREUM_API
        "opt-mainnet" -> BuildConfig.OPTIMISM_API
        "arb-mainnet" -> BuildConfig.ARBITRUM_API
        "polygon-mainnet" -> BuildConfig.POLYGON_API
        else -> ""
    }
}