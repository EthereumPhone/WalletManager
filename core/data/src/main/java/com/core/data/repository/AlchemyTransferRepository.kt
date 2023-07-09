package com.core.data.repository

import com.core.data.model.requestBody.NetworkTransferRequestBody
import com.core.data.remote.TransfersApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TransferDao
import com.core.database.model.TransferEntity
import com.core.database.model.asExternalModel
import com.core.model.NetworkChain
import com.core.model.Transfer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlchemyTransferRepository @Inject constructor(
    private val transferDao: TransferDao,
    private val transfersApi: TransfersApi
): TransferRepository {
    override fun getTransfers(): Flow<List<Transfer>> =
        transferDao.getTransfers()
            .map { it.map(TransferEntity::asExternalModel) }

    override fun getTransfers(chainId: Int): Flow<List<Transfer>> =
        transferDao.getTransfers(chainId)
            .map { it.map(TransferEntity::asExternalModel) }

    override fun getTransfers(categories: List<String>): Flow<List<Transfer>> =
        transferDao.getTransfers(categories)
            .map { it.map(TransferEntity::asExternalModel) }


    override fun getTransfers(
        chainId: Int,
        categories: List<String>
    ): Flow<List<Transfer>> =
        transferDao.getTransfers(chainId, categories)
            .map { it.map(TransferEntity::asExternalModel) }

    override suspend fun refreshTransfers(toAddress: String) {
        withContext(Dispatchers.IO) {
            val networks = NetworkChain.getAllNetworkChains()
            networks.map { network ->
                val apiKey = chainToApiKey(network.chainName)
                async {
                    val transfers = transfersApi.getTransfers(
                        network = network.chainName,
                        apiKey = apiKey,
                        requestBody = NetworkTransferRequestBody(
                            params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                                toAddress = toAddress
                            ))
                        )
                    ).map { it.asEntity(
                        chainId = network.chainId,
                        userIsSender = toAddress == it.from
                    ) }
                    transferDao.insertTransfers(transfers)
                }
            }
        }
    }
}