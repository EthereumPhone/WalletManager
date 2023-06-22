package com.core.data.repository

import com.core.Resource.Resource
import com.core.data.BuildConfig
import com.core.data.model.requestBody.NetworkTransferRequestBody
import com.core.data.remote.TransfersApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TransferDao
import com.core.database.model.asExternalModel
import com.core.model.NetworkChain
import com.core.model.EntryCategory
import com.core.model.Transfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ethereumphone.walletsdk.WalletSDK
import retrofit2.HttpException
import java.io.IOException

class AlchemyTransferRepository(
    private val walletSDK: WalletSDK,
    private val transferDao: TransferDao,
    private val transfersApi: TransfersApi
): TransferRepository {
    override fun getAllNetworksTransfers(fetchRemote: Boolean): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading(true))
        val localTransfers = transferDao.getAllTransfers()
        emit(Resource.Success(
            data = localTransfers.map { it.asExternalModel() }
        ))

        val isDbEmpty = localTransfers.isEmpty()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        val networks = NetworkChain.getAllNetworkChains()

        networks.forEach { networkChain ->
            val apiKey = chainToApiKey(networkChain.chainName)
            if(apiKey.isEmpty()) {
                return@forEach
            }
            val remoteTransfers = try {
                transfersApi.getTransfers(
                    network = networkChain.chainName,
                    apiKey = apiKey,
                    requestBody = NetworkTransferRequestBody(params = listOf(
                        NetworkTransferRequestBody.NetworkTransferRequestParams(
                            toAddress = walletSDK.getAddress()
                        ))
                    )
                )
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("parse error"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't finalize request for network: ${networkChain.chainName}"))
                null
            }
            remoteTransfers?.let { transfers ->
                transferDao.insertTransfers(transfers.map { it.asEntity(networkChain.chainId) })
            }
        }
        emit(Resource.Success(
            transferDao.getAllTransfers().map { it.asExternalModel() }
        ))

        emit(Resource.Loading(false))
    }

    override fun getAllTransfersByChainId(
        fetchRemote: Boolean,
        chainId: Int
    ): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading(true))
        val localTransfers = transferDao.getAllTransfersByChain(chainId)
        emit(Resource.Success(
            data = localTransfers.map { it.asExternalModel() }
        ))

        val isDbEmpty = localTransfers.isEmpty()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        val network = NetworkChain.getNetworkByChainId(chainId)
        val apiKey = chainToApiKey(network?.chainName.orEmpty())

        if (network == null || apiKey.isEmpty()) {
            emit(Resource.Loading(false))
            return@flow
        }

        val remoteTransfers = try {
            transfersApi.getTransfers(
                network = network.chainName,
                apiKey = apiKey,
                requestBody = NetworkTransferRequestBody(params = listOf(
                    NetworkTransferRequestBody.NetworkTransferRequestParams(
                        toAddress = walletSDK.getAddress()
                    ))
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            emit(Resource.Error("parse error"))
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(Resource.Error("network error"))
            null
        }

        remoteTransfers?.let { transfers ->
            transferDao.insertTransfers(transfers.map { it.asEntity(network.chainId) })
            emit(Resource.Success(
                data = transferDao.getAllTransfersByChain(chainId).map { it.asExternalModel() }
            ))
        }
        emit(Resource.Loading(false))
    }

    override fun getAllTransfersByCategories(
        fetchRemote: Boolean,
        categories: List<EntryCategory>
    ): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading(true))
        val localTransfers = transferDao.getAllTransfersByCategories(categories.map { it.toString() })
        emit(Resource.Success(
            data = localTransfers.map { it.asExternalModel() }
        ))

        val isDbEmpty = localTransfers.isEmpty()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        val networks = NetworkChain.getAllNetworkChains()

        networks.forEach { networkChain ->
            val apiKey = chainToApiKey(networkChain.chainName)
            if(apiKey.isEmpty()) {
                return@forEach
            }
            val remoteTransfers = try {
                transfersApi.getTransfers(
                    network = networkChain.chainName,
                    apiKey = apiKey,
                    requestBody = NetworkTransferRequestBody(params = listOf(
                        NetworkTransferRequestBody.NetworkTransferRequestParams(
                            toAddress = walletSDK.getAddress(),
                            category = categories.map { it.toString() }
                        ))
                    )
                )
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("parse error"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't finalize request for network: ${networkChain.chainName}"))
                null
            }
            remoteTransfers?.let { transfers ->
                transferDao.insertTransfers(transfers.map { it.asEntity(networkChain.chainId) })
            }
        }
        emit(Resource.Success(
            transferDao.getAllTransfersByCategories(categories.map { it.toString() }).map { it.asExternalModel() }
        ))

        emit(Resource.Loading(false))
    }

    override fun getAllTransfersByChainAndCategories(
        fetchRemote: Boolean,
        chainId: Int,
        categories: List<EntryCategory>
    ): Flow<Resource<List<Transfer>>> = flow {
        emit(Resource.Loading(true))
        val localTransfers = transferDao.getAllTransfersByChainAndCategories(chainId, categories.map { it.toString() })
        emit(Resource.Success(
            data = localTransfers.map { it.asExternalModel() }
        ))

        val isDbEmpty = localTransfers.isEmpty()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        val network = NetworkChain.getNetworkByChainId(chainId)
        val apiKey = chainToApiKey(network?.chainName.orEmpty())

        if (network == null || apiKey.isEmpty()) {
            emit(Resource.Loading(false))
            return@flow
        }

        val remoteTransfers = try {
            transfersApi.getTransfers(
                network = network.chainName,
                apiKey = apiKey,
                requestBody = NetworkTransferRequestBody(params = listOf(
                    NetworkTransferRequestBody.NetworkTransferRequestParams(
                        toAddress = walletSDK.getAddress(),
                        category = categories.map { it.toString() }
                    ))
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            emit(Resource.Error("parse error"))
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(Resource.Error("network error"))
            null
        }

        remoteTransfers?.let { transfers ->
            transferDao.insertTransfers(transfers.map { it.asEntity(network.chainId) })
            emit(Resource.Success(
                data = transferDao.getAllTransfersByChainAndCategories(chainId, categories.map { it.toString() })
                    .map { it.asExternalModel() }
            ))
        }
        emit(Resource.Loading(false))
    }
}