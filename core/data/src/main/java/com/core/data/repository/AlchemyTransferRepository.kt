package com.core.data.repository

import android.util.Log
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

    override suspend fun refreshTransfers(address: String) {
        Log.d("Transfer API", "update started")

        withContext(Dispatchers.IO) {
            val networks = NetworkChain.getAllNetworkChains()
            networks
                .filter { it != NetworkChain.BASE }
                .map { network ->
                val apiKey = chainToApiKey(network.chainName)
                    // outbound transactions
                async {
                    val transfers = transfersApi.getTransfers(
                        "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                        requestBody = NetworkTransferRequestBody(
                            params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                                fromAddress = address,
                                category = listOf("external")
                            ))
                        )
                    ).result.transfers.map {
                        it.asEntity(
                            chainId = network.chainId,
                            userIsSender = address.equals(it.from,true),
                        )
                    }
                    transferDao.insertTransfers(transfers)
                }

                    // inbound transactions
                async {
                    val transfers = transfersApi.getTransfers(
                        "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                        requestBody = NetworkTransferRequestBody(
                            params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                                toAddress = address,
                                category = listOf("external")
                            ))
                        )
                    ).result.transfers.map {
                        it.asEntity(
                            chainId = network.chainId,
                            userIsSender = address.equals(it.from,true),
                        )
                    }
                    transferDao.insertTransfers(transfers)
                }
            }
        }
    }

    override suspend fun refreshTransfersByNetwork(address: String, network: NetworkChain) {
        val apiKey = chainToApiKey(network.chainName)

        withContext(Dispatchers.IO) {
            // inbound transactions
            async {
                val transfers = transfersApi.getTransfers(
                    "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                    requestBody = NetworkTransferRequestBody(
                        params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                            fromAddress = address,
                            category = listOf("external")
                        ))
                    )
                ).result.transfers.map {
                    it.asEntity(
                        chainId = network.chainId,
                        userIsSender = address.equals(it.from,true),
                    )
                }
                transferDao.insertTransfers(transfers)
            }
            // outbound transactions
            async {
                val transfers = transfersApi.getTransfers(
                    "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                    requestBody = NetworkTransferRequestBody(
                        params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                            toAddress = address,
                            category = listOf("external")
                        ))
                    )
                ).result.transfers.map {
                    it.asEntity(
                        chainId = network.chainId,
                        userIsSender = address.equals(it.from,true),
                    )
                }
                transferDao.insertTransfers(transfers)
            }
        }
    }
}