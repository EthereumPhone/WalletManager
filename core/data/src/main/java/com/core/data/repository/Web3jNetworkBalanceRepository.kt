package com.core.data.repository

import com.core.Resource.Resource
import com.core.data.remote.NetworkBalanceApi
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class Web3jNetworkBalanceRepository(
    private val networkBalanceApi: NetworkBalanceApi,
    private val tokenBalanceDao: TokenBalanceDao
): NetworkBalanceRepository {

    /**
     * I intend to reuse the Token Balance entity for the Network currency,
     * to distinguish them from the erc20 tokens, The address of the token will the same as the chainID,
     * For instance; USDC-mainnet address: "<some hex sequence>", mainnet eth address = "1"
     */
    override fun getNetworkBalanceByChainId(
        fetchRemote: Boolean,
        toAddress: String,
        chainId: Int
    ): Flow<Resource<TokenBalance>> = flow {
        emit(Resource.Loading(true))

        val localNetworkBalance = tokenBalanceDao.getBalanceByContractAddress(chainId.toString())

        localNetworkBalance?.let {
            emit(Resource.Success(
                data = it.asExternalModule()
            ))
        }

        val isDbEmpty = localNetworkBalance == null
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        val network = NetworkChain.getNetworkByChainId(chainId) ?: return@flow

        val remoteNetworkBalance = try {
            networkBalanceApi.getNetworkCurrency(
                toAddress,
                network.rpc)
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(Resource.Error("Couldn't finalize request for network: ${network.chainName}"))
            null
        }

        remoteNetworkBalance?.let {
            tokenBalanceDao.upsertTokenBalances(
                listOf(TokenBalanceEntity(
                    contractAddress = network.chainId.toString(),
                    chainId = network.chainId,
                    tokenBalance = remoteNetworkBalance.toDouble()
                ))
            )
            emit(Resource.Success(
                data = tokenBalanceDao.getBalanceByContractAddress(chainId.toString())?.asExternalModule()
            ))
        }
        emit(Resource.Loading(false))
    }

    override fun getAllNetworkBalance(
        fetchRemote: Boolean,
        toAddress: String,
    ): Flow<Resource<List<TokenBalance>>> = flow {
        emit(Resource.Loading(true))

        val networks = NetworkChain.getAllNetworkChains()

        val localNetworkBalances = tokenBalanceDao.getBalancesByContractAddresses(networks.map { it.chainId.toString() })
        emit(Resource.Success(
            data = localNetworkBalances.map { it.asExternalModule() }
        ))

        val isDbEmpty = localNetworkBalances.isEmpty()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        networks.forEach { networkChain ->
            val remoteNetworkBalance = try {
                networkBalanceApi.getNetworkCurrency(
                    networkChain.chainName,
                    networkChain.rpc
                )
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't finalize request for network: ${networkChain.chainName}"))
                null
            }
            remoteNetworkBalance?.let {
                tokenBalanceDao.upsertTokenBalances(
                    listOf(TokenBalanceEntity(
                        contractAddress = networkChain.chainId.toString(),
                        chainId = networkChain.chainId,
                        tokenBalance = remoteNetworkBalance.toDouble()
                    ))
                )
            }
        }
        val newNetworkBalances = tokenBalanceDao.getBalancesByContractAddresses(networks.map { it.chainId.toString() })

        emit(Resource.Success(
            data = newNetworkBalances.map { it.asExternalModule() }
        ))
        emit(Resource.Loading(false))
    }
}