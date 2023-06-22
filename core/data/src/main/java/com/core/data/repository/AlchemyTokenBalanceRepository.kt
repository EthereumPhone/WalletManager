package com.core.data.repository

import com.core.Resource.Resource
import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenBalanceRequestBody
import com.core.data.remote.TokenBalanceApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenBalance
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class AlchemyTokenBalanceRepository(
    private val tokenBalanceApi: TokenBalanceApi,
    private val tokenBalanceDao: TokenBalanceDao
): TokenBalanceRepository {
    override fun getAllTokenBalances(
        fetchRemote: Boolean,
        toAddress: String
        ): Flow<Resource<List<TokenBalance>>> = flow {
        emit(Resource.Loading(true))

        val localMetadata = tokenBalanceDao.getAllBalances()
        emit(Resource.Success(
            data = localMetadata.map { it.asExternalModule() }
        ))

        val isDbEmpty = localMetadata.isEmpty()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }
        val networks = NetworkChain.getAllNetworkChains()

        networks.forEach { networkChain ->
            val apiKey = chainToApiKey(networkChain.chainName)
            if (apiKey.isEmpty()) {
                return@forEach
            }
            val remoteTokenBalance = try {
                tokenBalanceApi.getTokenBalances(
                    network = networkChain.chainName,
                    apiKey = apiKey,
                    requestBody = TokenBalanceRequestBody.allErc20Tokens(toAddress)
                ).map { it.asEntity(networkChain.chainId) }

            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("parse error"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't finalize request for network: ${networkChain.chainName}"))
                null
            }
            remoteTokenBalance?.let {
                tokenBalanceDao.upsertTokenBalances(remoteTokenBalance)
            }
        }
        emit(Resource.Success(
            data = tokenBalanceDao.getAllBalances().map { it.asExternalModule() }
        ))
        emit(Resource.Loading(false))
    }

    override fun getTokenBalanceByContractAddress(
        fetchRemote: Boolean,
        toAddress: String,
        contractAddress: List<TokenBalance>
    ): Flow<Resource<List<TokenBalance>>> = flow {
        emit(Resource.Loading(true))

        val addresses = contractAddress.map { it.contractAddress }

        val localMetadata = addresses.map {  tokenBalanceDao.getBalanceByContractAddress(it) }

        emit(Resource.Success(
            data = localMetadata.filterNotNull().map { it.asExternalModule() }
        ))

        val isDbEmpty = localMetadata.isEmpty()
        val shouldJustLoadFromCache = !isDbEmpty && !fetchRemote
        if (shouldJustLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        val networks = NetworkChain.getAllNetworkChains()
        networks.forEach { networkChain ->
            val apiKey = chainToApiKey(networkChain.chainName)
            if (apiKey.isEmpty()) {
                return@forEach
            }
            val remoteTokenBalance = try {
                tokenBalanceApi.getTokenBalances(
                    network = networkChain.chainName,
                    apiKey = apiKey,
                    requestBody = TokenBalanceRequestBody.forContract(
                        toAddress = toAddress,
                        contractAddress = contractAddress
                            .filter{ it.chainId == networkChain.chainId }
                            .map { it.contractAddress }
                        )
                ).map { it.asEntity(networkChain.chainId) }
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("parse error"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't finalize request for network: ${networkChain.chainName}"))
                null
            }

            if (remoteTokenBalance != null) {
                tokenBalanceDao.upsertTokenBalances(remoteTokenBalance)
            }
        }

        val updatedTokenBalance = addresses.map { tokenBalanceDao.getBalanceByContractAddress(it) }
        emit(Resource.Success(
            data = updatedTokenBalance.filterNotNull().map { it.asExternalModule() }
        ))
        emit(Resource.Loading(false))
    }
}