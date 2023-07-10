package com.core.data.repository

import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.data.remote.TokenMetadataApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.model.erc20.asExternalModel
import com.core.model.NetworkChain
import com.core.model.TokenMetadata
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


    override suspend fun refreshTokensMetadata(
        contractAddresses: List<String>,
        network: Int
    ) {
        val network = NetworkChain.getNetworkByChainId(network)?: return
        val apiKey = chainToApiKey(network.chainName)

        val metadataList = tokenMetadataApi
            .getTokenMetadata(
                "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                TokenMetadataRequestBody(params = contractAddresses)
            )

        // TODO(Check if address is mapped to right contract)
        val filledMetadata = contractAddresses.zip(metadataList).map { (address, tokenMetadata) ->
            tokenMetadata.asEntity(
                contractAddress = address,
                chainId = network.chainId
            )
        }
        tokenMetadataDao.upsertTokensMetadata(filledMetadata)
    }
}