package org.ethereumphone.walletmanager.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.core.database.dao.WalletDataDao
import org.ethereumphone.walletmanager.core.database.model.WalletDataEntity
import org.ethereumphone.walletmanager.core.domain.repository.NetworkWalletDataRepository
import org.ethereumphone.walletmanager.core.util.Resource
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import javax.inject.Inject

class NetworkWalletDataRepositoryImplementation @Inject constructor(
    private val walletSDK: WalletSDK,
    private val web3j: Web3j,
    private val walletDataDao: WalletDataDao
): NetworkWalletDataRepository {
    override fun getWalletChainId(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        emit(Resource.Success(walletSDK.getChainId()))
    }

    override fun getWalletAddress(): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val walletAddress = walletDataDao.getNetworkDataByChainId(1).address
        emit(Resource.Loading(walletAddress))

        try {
            val address = walletSDK.getAddress()
            // updates all network to newest address
            if(address != walletAddress) {
                val networks = walletDataDao.getAllNetworksData()
                networks.map {
                    val updatedNetwork = WalletDataEntity(
                        chainId = it.chainId,
                        name = it.name,
                        address = address,
                        rpc = it.rpc,
                        networkBalance = it.networkBalance
                    )
                    walletDataDao.upsertNetworkInformation(updatedNetwork)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    "Address couldn't be appended from the WalletSDK",
                    walletAddress
                )
            )
        }
        val newWalletAddress = walletDataDao.getNetworkDataByChainId(1).address
        emit(Resource.Success(newWalletAddress))
    }

    override fun getNetworkAmount(chainId: Int): Flow<Resource<Double>> = flow {
        emit(Resource.Loading())
        val networkData = walletDataDao.getNetworkDataByChainId(chainId)
        emit(Resource.Loading(networkData.networkBalance))

        try {
            val address = walletSDK.getAddress()
            val remote = web3j.ethGetBalance(
                address,
                DefaultBlockParameterName.LATEST
            ).send()

            walletDataDao.upsertNetworkInformation(
                WalletDataEntity(
                    chainId= networkData.chainId,
                    name = networkData.name,
                    rpc = networkData.rpc,
                    address = networkData.address,
                    networkBalance = remote.balance.toDouble()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()

            emit(Resource.Error(
                message = "Network Error",
                data = networkData.networkBalance
            ))
        }

        val newNetworkData = walletDataDao.getNetworkDataByChainId(chainId)
        emit(Resource.Success(newNetworkData.networkBalance))
    }

    override fun changeChain(chainId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val network = walletDataDao.getNetworkDataByChainId(chainId)
                walletSDK.changeChain(network.chainId, network.rpc)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(
                    "walletSDK couldn't switch to this network",
                    false
                ))
            }
        }
        emit(Resource.Success(true))
    }

}


/**
 * val defaultRpcs = when(chainId) {
 *             1 -> "https://rpc.ankr.com/eth" // main
 *             5 -> "https://rpc.ankr.com/eth_goerli" // goerli
 *             10 -> "https://rpc.ankr.com/optimism" // optimism
 *             100 -> "https://rpc.ankr.com/gnosis" // Gnosis
 *             137 -> "https://rpc.ankr.com/polygon" // polygon mainnet
 *             420 -> "https://rpc.ankr.com/optimism_testnet" // optimism testnet
 *             1101 -> "https://rpc.ankr.com/polygon_zkevm" // polygon zkEVM
 *             1142 -> "https://rpc.ankr.com/polygon_zkevm_testnet" // Polygon zkEVM Testnet
 *             8453 -> "https://rpc.ankr.com/base_goerli" // base testnet
 *             42161 -> "https://rpc.ankr.com/arbitrum" // arbitrum
 *             80001 -> "https://rpc.ankr.com/polygon_mumbai" // polygon testnet
 *             421611 -> "https://rpc.ankr.com/arbitrumnova" // arbitrum nova
 *             534351 -> "https://rpc.ankr.com/scroll_testnet" // scroll testnet
 *             534352 -> "" // scroll
 *             else -> ""
 *         }
 */