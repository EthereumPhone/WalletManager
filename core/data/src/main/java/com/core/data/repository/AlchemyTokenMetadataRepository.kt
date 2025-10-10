package com.core.data.repository

import android.util.Log
import com.core.data.model.dto.TokenMetadataDto
import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.data.remote.RetrofitClankerTokenApi
import com.core.data.remote.TokenMetadataApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.model.erc20.asExternalModel
import com.core.model.NetworkChain
import com.core.model.TokenMetadata
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlchemyTokenMetadataRepository @Inject constructor(
    private val tokenMetadataDao: TokenMetadataDao,
    private val tokenGroupDao: TokenGroupDao,
    private val tokenMetadataApi: TokenMetadataApi,
    private val clankerTokenApi: RetrofitClankerTokenApi
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
            val tokenGroups = mutableListOf<TokenGroupEntity>()
            val metadataList = contractAddresses.mapNotNull { address ->

                try {
                    Log.d("refreshTokensMetadata", address)
                    val response = tokenMetadataApi
                        .getTokenMetadata(
                            "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                            TokenMetadataRequestBody(params = listOf(address))
                        )
                    
                    // Check if the response has empty name and symbol, use Clanker API as fallback
                    val tokenMetadata = if (response.result.name.isBlank() && response.result.symbol.isBlank()) {
                        Log.d("refreshTokensMetadata", "Empty metadata from Alchemy, trying Clanker API for $address")
                        try {
                            val clankerTokens = clankerTokenApi.getClankerTokens(
                                query = address,
                                limit = 1,
                                pageIndex = 1,
                                startAfter = ""
                            )
                            
                            if (clankerTokens.isNotEmpty()) {
                                val clankerToken = clankerTokens.first()
                                Log.d("refreshTokensMetadata", "Found token in Clanker: ${clankerToken.name}")

                                // Replace gateway.pinata.cloud with ipfs.io if present
                                val logoUrl = clankerToken.imgUrl
                                    ?.takeIf { it.isNotBlank() }
                                    ?.replace("gateway.pinata.cloud", "ipfs.io")

                                // Create TokenMetadataDto from ClankerToken
                                TokenMetadataDto(
                                    name = clankerToken.name,
                                    symbol = clankerToken.symbol,
                                    decimals = 18, // Default to 18 decimals for Clanker tokens
                                    logo = logoUrl
                                )
                            } else {
                                Log.d("refreshTokensMetadata", "No token found in Clanker API for $address")
                                response.result
                            }
                        } catch (e: Exception) {
                            Log.e("refreshTokensMetadata", "Exception fetching from Clanker API for $address", e)
                            response.result
                        }
                    } else {
                        response.result
                    }
                    
                    // Generate group ID for this token
                    val groupId = generateGroupId(chainId, address)
                    
                    // Create token group entity
                    val tokenGroup = TokenGroupEntity(
                        groupId = groupId,
                        canonicalChainId = chainId,
                        canonicalAddress = address.lowercase(),
                        symbol = tokenMetadata.symbol,
                        name = tokenMetadata.name
                    )
                    tokenGroups.add(tokenGroup)
                    
                    // Create token metadata with group ID
                    tokenMetadata.asEntity(
                        contractAddress = address,
                        chainId = chainId,
                        groupId = groupId
                    )
                } catch (e: JsonDataException) {
                    // This happens when the API returns an error object instead of result
                    Log.w("refreshTokensMetadata", "Token metadata not found for $address: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("refreshTokensMetadata", "Exception fetching metadata for $address", e)
                    null
                }
            }

            // Insert token groups first (in case of foreign key constraints)
            if (tokenGroups.isNotEmpty()) {
                tokenGroupDao.upsertTokenGroups(tokenGroups)
            }
            
            // Then insert token metadata
            tokenMetadataDao.upsertTokensMetadata(metadataList)


        }
    }

    override suspend fun refreshTokensMetadataByNetwork(contractAddresses: List<String>, network: NetworkChain) {
        val apiKey = chainToApiKey(network.chainName)

        withContext(Dispatchers.IO) {
            val tokenGroups = mutableListOf<TokenGroupEntity>()
            val metadataList = contractAddresses.mapNotNull { address ->
                try {
                    val response = tokenMetadataApi
                        .getTokenMetadata(
                            "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                            TokenMetadataRequestBody(params = listOf(address))
                        )
                    
                    // Check if the response has empty name and symbol, use Clanker API as fallback
                    val tokenMetadata = if (response.result.name.isBlank() && response.result.symbol.isBlank()) {
                        Log.d("refreshTokensMetadataByNetwork", "Empty metadata from Alchemy, trying Clanker API for $address")
                        try {
                            val clankerTokens = clankerTokenApi.getClankerTokens(
                                query = address,
                                limit = 1,
                                pageIndex = 1,
                                startAfter = ""
                            )
                            
                            if (clankerTokens.isNotEmpty()) {
                                val clankerToken = clankerTokens.first()
                                Log.d("refreshTokensMetadataByNetwork", "Found token in Clanker: ${clankerToken.name}")
                                
                                // Replace gateway.pinata.cloud with ipfs.io if present
                                val logoUrl = clankerToken.imgUrl
                                    ?.takeIf { it.isNotBlank() }
                                    ?.replace("gateway.pinata.cloud", "ipfs.io")
                                
                                // Create TokenMetadataDto from ClankerToken
                                TokenMetadataDto(
                                    name = clankerToken.name,
                                    symbol = clankerToken.symbol,
                                    decimals = 18, // Default to 18 decimals for Clanker tokens
                                    logo = logoUrl
                                )
                            } else {
                                Log.d("refreshTokensMetadataByNetwork", "No token found in Clanker API for $address")
                                response.result
                            }
                        } catch (e: Exception) {
                            Log.e("refreshTokensMetadataByNetwork", "Exception fetching from Clanker API for $address", e)
                            response.result
                        }
                    } else {
                        response.result
                    }
                    
                    // Generate group ID for this token
                    val groupId = generateGroupId(network.chainId, address)
                    
                    // Create token group entity
                    val tokenGroup = TokenGroupEntity(
                        groupId = groupId,
                        canonicalChainId = network.chainId,
                        canonicalAddress = address.lowercase(),
                        symbol = tokenMetadata.symbol,
                        name = tokenMetadata.name
                    )
                    tokenGroups.add(tokenGroup)
                    
                    // Create token metadata with group ID
                    tokenMetadata.asEntity(
                        contractAddress = address,
                        chainId = network.chainId,
                        groupId = groupId
                    )
                } catch (e: JsonDataException) {
                    // This happens when the API returns an error object instead of result
                    Log.w("refreshTokensMetadataByNetwork", "Token metadata not found for $address: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("refreshTokensMetadataByNetwork", "Exception fetching metadata for $address", e)
                    null
                }
            }
            
            // Insert token groups first (in case of foreign key constraints)
            if (tokenGroups.isNotEmpty()) {
                tokenGroupDao.upsertTokenGroups(tokenGroups)
            }
            
            // Then insert token metadata
            tokenMetadataDao.upsertTokensMetadata(metadataList)
        }
    }


    override suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>) = tokenMetadataDao.upsertTokensMetadata(tokensMetadata)

    /**
     * Generate a group ID for a token.
     * Following the same pattern as TokenSeedingHelper:
     * - For mainnet (chainId = 1), use "mainnet_<address>"
     * - For other chains, use "group_<uuid>"
     */
    private fun generateGroupId(chainId: Int, address: String): String {
        return "${chainId}_${address.lowercase()}"
    }
}