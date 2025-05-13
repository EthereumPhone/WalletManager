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
        Log.d("Transfer API", "refreshTransfers update started for address: $address")

        withContext(Dispatchers.IO) {
            val networks = NetworkChain.getAllNetworkChains()
            Log.d("AlchemyTransferRepo", "refreshTransfers: Processing ${networks.size} networks initially.")
            networks
                .filter { it != NetworkChain.ZORA } // User confirmed this filter is intentional
                .forEach { network -> // Changed from .map to .forEach for more explicit logging blocks
                    val apiKey = chainToApiKey(network.chainName)
                    val isBaseNetwork = network.chainId == NetworkChain.BASE.chainId

                    if (isBaseNetwork) {
                        Log.i("AlchemyTransferRepo", "refreshTransfers: Processing BASE network (Chain ID: ${network.chainId})")
                    }

                    // Outbound transactions
                    async {
                        try {
                            Log.d("AlchemyTransferRepo", "refreshTransfers: Fetching OUTBOUND for ${network.chainName}")
                            val apiResponse = transfersApi.getTransfers(
                                "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                                requestBody = NetworkTransferRequestBody(
                                    params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                                        fromAddress = address,
                                        category = listOf("external")
                                    ))
                                )
                            )
                            val parsedDtos = apiResponse.result.transfers
                            if (isBaseNetwork) {
                                Log.i("AlchemyTransferRepo", "refreshTransfers: BASE OUTBOUND DTOs received: ${parsedDtos.size}")
                                parsedDtos.forEachIndexed { index, dto ->
                                    Log.d("AlchemyTransferRepo", "refreshTransfers: BASE OUTBOUND DTO[$index]: hash=${dto.hash}, value=${dto.value}, asset=${dto.asset}, from=${dto.from}, to=${dto.to}")
                                }
                            }

                            val transferEntities = parsedDtos.map { dto ->
                                val entity = dto.asEntity(
                                    chainId = network.chainId,
                                    userIsSender = address.equals(dto.from, true)
                                )
                                if (isBaseNetwork) {
                                    Log.d("AlchemyTransferRepo", "refreshTransfers: BASE OUTBOUND DTO mapped to Entity: hash=${entity.hash}, value=${entity.value}, asset=${entity.asset}, from=${entity.fromaddress}, to=${entity.toaddress}")
                                }
                                entity
                            }
                            if (isBaseNetwork) {
                                Log.i("AlchemyTransferRepo", "refreshTransfers: Inserting ${transferEntities.size} BASE OUTBOUND entities into DB.")
                            }
                            if (transferEntities.isNotEmpty()) {
                                transferDao.insertTransfers(transferEntities)
                                if (isBaseNetwork) {
                                    Log.i("AlchemyTransferRepo", "refreshTransfers: Insertion of ${transferEntities.size} BASE OUTBOUND entities - DAO call complete.")
                                }
                            } else if (isBaseNetwork) {
                                Log.w("AlchemyTransferRepo", "refreshTransfers: No BASE OUTBOUND entities to insert.")
                            }
                        } catch (e: Exception) {
                            Log.e("AlchemyTransferRepo", "refreshTransfers: ERROR during OUTBOUND for ${network.chainName}: ", e)
                        }
                    }

                    // Inbound transactions
                    async {
                        try {
                            Log.d("AlchemyTransferRepo", "refreshTransfers: Fetching INBOUND for ${network.chainName}")
                            val apiResponse = transfersApi.getTransfers(
                                "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                                requestBody = NetworkTransferRequestBody(
                                    params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                                        toAddress = address,
                                        category = listOf("external")
                                    ))
                                )
                            )
                            val parsedDtos = apiResponse.result.transfers
                            if (isBaseNetwork) {
                                Log.i("AlchemyTransferRepo", "refreshTransfers: BASE INBOUND DTOs received: ${parsedDtos.size}")
                                parsedDtos.forEachIndexed { index, dto ->
                                    Log.d("AlchemyTransferRepo", "refreshTransfers: BASE INBOUND DTO[$index]: hash=${dto.hash}, value=${dto.value}, asset=${dto.asset}, from=${dto.from}, to=${dto.to}")
                                }
                            }

                            val transferEntities = parsedDtos.map { dto ->
                                val entity = dto.asEntity(
                                    chainId = network.chainId,
                                    userIsSender = address.equals(dto.from, true)
                                )
                                if (isBaseNetwork) {
                                    Log.d("AlchemyTransferRepo", "refreshTransfers: BASE INBOUND DTO mapped to Entity: hash=${entity.hash}, value=${entity.value}, asset=${entity.asset}, from=${entity.fromaddress}, to=${entity.toaddress}")
                                }
                                entity
                            }
                            if (isBaseNetwork) {
                                Log.i("AlchemyTransferRepo", "refreshTransfers: Inserting ${transferEntities.size} BASE INBOUND entities into DB.")
                            }
                            if (transferEntities.isNotEmpty()) {
                                transferDao.insertTransfers(transferEntities)
                                if (isBaseNetwork) {
                                    Log.i("AlchemyTransferRepo", "refreshTransfers: Insertion of ${transferEntities.size} BASE INBOUND entities - DAO call complete.")
                                }
                            } else if (isBaseNetwork) {
                                Log.w("AlchemyTransferRepo", "refreshTransfers: No BASE INBOUND entities to insert.")
                            }
                        } catch (e: Exception) {
                            Log.e("AlchemyTransferRepo", "refreshTransfers: ERROR during INBOUND for ${network.chainName}: ", e)
                        }
                    }
                }
        }
        Log.d("Transfer API", "refreshTransfers update finished for address: $address")
    }

    override suspend fun refreshTransfersByNetwork(address: String, chainId: Int) {
        val network = NetworkChain.getNetworkByChainId(chainId)
        val apiKey = network?.let { chainToApiKey(it.chainName) }

        Log.d("AlchemyTransferRepo", "refreshTransfersByNetwork called for chainId: $chainId")

        withContext(Dispatchers.IO) {
            // inbound transactions
            async {
                try {
                    val transfers = transfersApi.getTransfers(
                        "https://${network!!.chainName}.g.alchemy.com/v2/$apiKey",
                        requestBody = NetworkTransferRequestBody(
                            params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                                fromAddress = address,
                                category = listOf("external")
                            ))
                        )
                    ).result.transfers
                    if (network.chainId == NetworkChain.BASE.chainId) {
                        Log.d("AlchemyTransferRepo", "Base inbound transfers count: ${transfers.size}")
                    }
                    val transferEntities = transfers.map {
                        it.asEntity(
                            chainId = network.chainId,
                            userIsSender = address.equals(it.from, true),
                        )
                    }
                    transferDao.insertTransfers(transferEntities)
                } catch (e: Exception) {
                    Log.e("AlchemyTransferRepo", "Error fetching/parsing inbound transfers for chainId $chainId: ", e)
                }
            }

            // outbound transactions
            async {
                try {
                    val transfers = transfersApi.getTransfers(
                        "https://${network!!.chainName}.g.alchemy.com/v2/$apiKey",
                        requestBody = NetworkTransferRequestBody(
                            params = listOf(NetworkTransferRequestBody.NetworkTransferRequestParams(
                                toAddress = address,
                                category = listOf("external")
                            ))
                        )
                    ).result.transfers
                    if (network.chainId == NetworkChain.BASE.chainId) {
                        Log.d("AlchemyTransferRepo", "Base outbound transfers count: ${transfers.size}")
                    }
                    val transferEntities = transfers.map {
                        it.asEntity(
                            chainId = network!!.chainId,
                            userIsSender = address.equals(it.from, true),
                        )
                    }
                    transferDao.insertTransfers(transferEntities)
                } catch (e: Exception) {
                    Log.e("AlchemyTransferRepo", "Error fetching/parsing outbound transfers for chainId $chainId: ", e)
                }
            }
        }
    }
}