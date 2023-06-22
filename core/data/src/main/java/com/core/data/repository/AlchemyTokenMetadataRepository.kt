package com.core.data.repository

import com.core.Resource.Resource
import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.data.remote.TokenMetadataApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.asExternalModel
import com.core.model.NetworkChain
import com.core.model.TokenBalance
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class AlchemyTokenMetadataRepository(
    private val tokenMetadataDao: TokenMetadataDao,
    private val tokenMetadataApi: TokenMetadataApi
): TokenMetadataRepository {

    override fun getTokenMetadataByContractAddress(
        fetchRemote: Boolean,
        contractAddress: List<TokenBalance>
    ): Flow<Resource<List<TokenMetadata>>> = flow {
        emit(Resource.Loading(true))

        val localTokenMetadata = tokenMetadataDao.getAllTokenMetadata()
        emit(Resource.Success(
            data = localTokenMetadata.map { it.asExternalModel() }
        ))

        val isDbEmpty = localTokenMetadata.isEmpty()
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

            val contractsToSearch = contractAddress
                .filter { it.chainId == networkChain.chainId }


            val remoteTokenMetadata = try {
                val response = tokenMetadataApi.getTokenMetadata(
                    network = networkChain.chainName,
                    apiKey = apiKey,
                    requestBody = TokenMetadataRequestBody(params = contractsToSearch.map { it.contractAddress }
                    )
                )

                response.zip(contractsToSearch).map { (response, contract) ->
                    response.asEntity(
                        contractAddress = contract.contractAddress,
                        chainId = networkChain.chainId
                    )
                }

            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("parse error"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't finalize request for network: ${networkChain.chainName}"))
                null
            }


            remoteTokenMetadata?.let {
                tokenMetadataDao.upsertTokenMetadata(remoteTokenMetadata)
            }
        }
        emit(Resource.Success(
            data = contractAddress.map {
                tokenMetadataDao.getTokenMetadataByContractAddress(it.contractAddress)
                    .asExternalModel()
            }
        ))
        emit(Resource.Loading(false))
    }
}