package com.core.data.repository

import android.util.Log
import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.data.remote.TokenMetadataApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.model.erc20.asExternalModel
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenMetadata
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlchemyTokenMetadataRepository @Inject constructor(
    private val tokenMetadataDao: TokenMetadataDao,
    private val tokenMetadataApi: TokenMetadataApi
): TokenMetadataRepository {
    override fun getTokensMetadata(): Flow<List<TokenMetadata>> =
        tokenMetadataDao.getTokensMetadata()
            .map { it.map(TokenMetadataEntity::asExternalModel) }

    override fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>> =
        tokenMetadataDao.getTokenMetadata(contractAddresses)
            .map { it.map(TokenMetadataEntity::asExternalModel) }

    override fun getTokensMetadataBySymbols(symbols: List<String>): Flow<List<TokenMetadata>> =
        tokenMetadataDao.getTokensMetadataBySymbols(symbols)
            .map { it.map(TokenMetadataEntity::asExternalModel) }

    override fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>> =
        tokenMetadataDao.getTokenMetadata(chainId)
            .map { it.map(TokenMetadataEntity::asExternalModel) }


    override suspend fun refreshTokensMetadata(
        contractAddresses: List<String>,
        chainId: Int
    ) {
        Log.d("TokenMetadata API", "update started")


        val network = NetworkChain.getNetworkByChainId(chainId)?: return
        val apiKey = chainToApiKey(network.chainName)

        withContext(Dispatchers.IO) {
            val metadataList = contractAddresses.mapNotNull { address ->
                try {
                    Log.d("refreshTokensMetadata", address)
                    val response = tokenMetadataApi
                        .getTokenMetadata(
                            "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                            TokenMetadataRequestBody(params = listOf(address))
                        )
                    response.result.asEntity(
                        contractAddress = address,
                        chainId = chainId
                    )
                } catch (e: com.squareup.moshi.JsonDataException) {
                    // This happens when the API returns an error object instead of result
                    Log.w("refreshTokensMetadata", "Token metadata not found for $address: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("refreshTokensMetadata", "Exception fetching metadata for $address", e)
                    null
                }
            }
            tokenMetadataDao.upsertTokensMetadata(metadataList)
        }
    }

    override suspend fun refreshTokensMetadataByNetwork(contractAddresses: List<String>, network: NetworkChain) {
        val apiKey = chainToApiKey(network.chainName)

        withContext(Dispatchers.IO) {
            val metadataList = contractAddresses.mapNotNull { address ->
                try {
                    val response = tokenMetadataApi
                        .getTokenMetadata(
                            "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                            TokenMetadataRequestBody(params = listOf(address))
                        )
                    response.result.asEntity(
                        contractAddress = address,
                        chainId = network.chainId
                    )
                } catch (e: com.squareup.moshi.JsonDataException) {
                    // This happens when the API returns an error object instead of result
                    Log.w("refreshTokensMetadataByNetwork", "Token metadata not found for $address: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("refreshTokensMetadataByNetwork", "Exception fetching metadata for $address", e)
                    null
                }
            }
            tokenMetadataDao.upsertTokensMetadata(metadataList)
        }
    }


    override suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>) = tokenMetadataDao.upsertTokensMetadata(tokensMetadata)

}