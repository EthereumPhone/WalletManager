package com.core.data.repository

import android.util.Log
import com.core.data.model.dto.TokenMetadataDto
import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.data.remote.RetrofitClankerTokenApi
import com.core.data.remote.TokenMetadataApi
import com.core.data.service.OnChainTokenMetadataFetcher
import com.core.data.util.chainIdToRPC
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlchemyTokenMetadataRepository @Inject constructor(
    private val tokenMetadataDao: TokenMetadataDao,
    private val tokenGroupDao: TokenGroupDao,
    private val tokenMetadataApi: TokenMetadataApi,
    private val clankerTokenApi: RetrofitClankerTokenApi,
    private val onChainTokenMetadataFetcher: OnChainTokenMetadataFetcher
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
                                // Use Alchemy's decimals (often correct even when name/symbol are blank),
                                // then try on-chain fetch, then fall back to 18
                                val resolvedDecimals = response.result.decimals
                                    ?: try {
                                        val rpcUrl = "https://${network.chainName}.g.alchemy.com/v2/$apiKey"
                                        onChainTokenMetadataFetcher.fetchTokenMetadata(address, chainId, rpcUrl)?.decimals
                                    } catch (e: Exception) {
                                        Log.w("refreshTokensMetadata", "Failed on-chain decimals fetch for $address", e)
                                        null
                                    }

                                TokenMetadataDto(
                                    name = clankerToken.name,
                                    symbol = clankerToken.symbol,
                                    decimals = resolvedDecimals,
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
                    
                    // Resolve or create a groupId for this token
                    val resolvedGroupId = resolveGroupId(
                        chainId = chainId,
                        address = address,
                        symbol = tokenMetadata.symbol
                    )

                    // Ensure the TokenGroup exists only if needed
                    val existingGroup = tokenGroupDao.getGroupedToken(resolvedGroupId)
                    if (existingGroup == null) {
                        val tokenGroup = TokenGroupEntity(
                            groupId = resolvedGroupId,
                            canonicalChainId = chainId,
                            canonicalAddress = address.lowercase(),
                            symbol = tokenMetadata.symbol,
                            name = tokenMetadata.name
                        )
                        tokenGroups.add(tokenGroup)
                    }

                    // If decimals is still null, try on-chain fetch before storing
                    val finalMetadata = if (tokenMetadata.decimals == null) {
                        Log.d("refreshTokensMetadata", "Decimals null for $address, trying on-chain fetch")
                        val onChainDecimals = try {
                            val rpcUrl = "https://${network.chainName}.g.alchemy.com/v2/$apiKey"
                            onChainTokenMetadataFetcher.fetchTokenMetadata(address, chainId, rpcUrl)?.decimals
                        } catch (e: Exception) {
                            Log.w("refreshTokensMetadata", "Failed on-chain decimals fetch for $address", e)
                            null
                        }
                        tokenMetadata.copy(decimals = onChainDecimals)
                    } else {
                        tokenMetadata
                    }

                    // Create token metadata with resolved group ID
                    finalMetadata.asEntity(
                        contractAddress = address,
                        chainId = chainId,
                        groupId = resolvedGroupId
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
                                // Use Alchemy's decimals (often correct even when name/symbol are blank),
                                // then try on-chain fetch, then fall back to 18
                                val resolvedDecimals = response.result.decimals
                                    ?: try {
                                        val rpcUrl = "https://${network.chainName}.g.alchemy.com/v2/$apiKey"
                                        onChainTokenMetadataFetcher.fetchTokenMetadata(address, network.chainId, rpcUrl)?.decimals
                                    } catch (e: Exception) {
                                        Log.w("refreshTokensMetadataByNetwork", "Failed on-chain decimals fetch for $address", e)
                                        null
                                    }

                                TokenMetadataDto(
                                    name = clankerToken.name,
                                    symbol = clankerToken.symbol,
                                    decimals = resolvedDecimals,
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
                    
                    // Resolve or create a groupId for this token
                    val resolvedGroupId = resolveGroupId(
                        chainId = network.chainId,
                        address = address,
                        symbol = tokenMetadata.symbol
                    )

                    // Ensure the TokenGroup exists only if needed
                    val existingGroup = tokenGroupDao.getGroupedToken(resolvedGroupId)
                    if (existingGroup == null) {
                        val tokenGroup = TokenGroupEntity(
                            groupId = resolvedGroupId,
                            canonicalChainId = network.chainId,
                            canonicalAddress = address.lowercase(),
                            symbol = tokenMetadata.symbol,
                            name = tokenMetadata.name
                        )
                        tokenGroups.add(tokenGroup)
                    }

                    // If decimals is still null, try on-chain fetch before storing
                    val finalMetadata = if (tokenMetadata.decimals == null) {
                        Log.d("refreshTokensMetadataByNetwork", "Decimals null for $address, trying on-chain fetch")
                        val onChainDecimals = try {
                            val rpcUrl = "https://${network.chainName}.g.alchemy.com/v2/$apiKey"
                            onChainTokenMetadataFetcher.fetchTokenMetadata(address, network.chainId, rpcUrl)?.decimals
                        } catch (e: Exception) {
                            Log.w("refreshTokensMetadataByNetwork", "Failed on-chain decimals fetch for $address", e)
                            null
                        }
                        tokenMetadata.copy(decimals = onChainDecimals)
                    } else {
                        tokenMetadata
                    }

                    // Create token metadata with resolved group ID
                    finalMetadata.asEntity(
                        contractAddress = address,
                        chainId = network.chainId,
                        groupId = resolvedGroupId
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

    private suspend fun resolveGroupId(chainId: Int, address: String, symbol: String): String {
        // 1) Prefer explicit bridge relationships
        val byBridge = tokenGroupDao.findGroupIdByBridge(chainId, address)
        if (byBridge != null) return byBridge

        // 2) Fallback: prefer existing group by symbol (favor mainnet canonical groups)
        val bySymbol = tokenGroupDao.findGroupIdBySymbolPreferMainnet(symbol)
        if (bySymbol != null) return bySymbol

        // 3) Otherwise, create a new group id scoped to this token
        return generateGroupId(chainId, address)
    }

    override suspend fun reconcileTokenGroups() {
        withContext(Dispatchers.IO) {
            try {
                // Load all tokens and group them by symbol
                val allTokens = tokenMetadataDao.getTokensMetadata().first()
                val bySymbol = allTokens.groupBy { it.symbol.lowercase() }

                val groupsToCreate = mutableListOf<TokenGroupEntity>()
                val tokensToUpdate = mutableListOf<TokenMetadataEntity>()

                bySymbol.forEach { (_, tokenList) ->
                    if (tokenList.isEmpty()) return@forEach

                    val symbol = tokenList.first().symbol

                    // Prefer an existing mainnet-backed group if present
                    val preferredGroupId = tokenGroupDao.findGroupIdBySymbolPreferMainnet(symbol)

                    val canonicalToken = tokenList.minWithOrNull(compareBy<TokenMetadataEntity> { it.chainId != 1 }.thenBy { it.contractAddress.lowercase() })
                        ?: tokenList.first()

                    val canonicalGroupId = preferredGroupId ?: generateGroupId(canonicalToken.chainId, canonicalToken.contractAddress)

                    // Ensure the canonical TokenGroup exists
                    val existingGroup = tokenGroupDao.getGroupedToken(canonicalGroupId)
                    if (existingGroup == null) {
                        groupsToCreate.add(
                            TokenGroupEntity(
                                groupId = canonicalGroupId,
                                canonicalChainId = canonicalToken.chainId,
                                canonicalAddress = canonicalToken.contractAddress.lowercase(),
                                symbol = canonicalToken.symbol,
                                name = canonicalToken.name
                            )
                        )
                    }

                    // Update all tokens to point to the canonical group
                    tokenList.forEach { token ->
                        if (token.groupId != canonicalGroupId) {
                            tokensToUpdate.add(token.copy(groupId = canonicalGroupId))
                        }
                    }
                }

                if (groupsToCreate.isNotEmpty()) tokenGroupDao.upsertTokenGroups(groupsToCreate)
                if (tokensToUpdate.isNotEmpty()) tokenMetadataDao.upsertTokensMetadata(tokensToUpdate)

            } catch (e: Exception) {
                Log.e("AlchemyTokenMetadataRepository", "Error reconciling token groups", e)
            }
        }
    }

    override suspend fun lookupTokenByAddress(contractAddress: String, chainId: Int): TokenMetadata? {
        return withContext(Dispatchers.IO) {
            val normalizedAddress = contractAddress.lowercase()

            // First, check if we already have this token in the database
            try {
                val existingTokens = tokenMetadataDao.getTokenMetadata(listOf(normalizedAddress)).first()
                val existingToken = existingTokens.firstOrNull {
                    it.contractAddress.equals(normalizedAddress, ignoreCase = true) &&
                    it.chainId == chainId
                }

                if (existingToken != null) {
                    return@withContext existingToken.asExternalModel()
                }
            } catch (e: Exception) {
                Log.w("AlchemyTokenMetadataRepository", "Error checking local database", e)
            }

            // Fetch on-chain if not in database
            try {
                val rpcUrl = chainIdToRPC(chainId)
                val onChainMetadata = onChainTokenMetadataFetcher.fetchTokenMetadata(
                    contractAddress = normalizedAddress,
                    chainId = chainId,
                    rpcUrl = rpcUrl
                )

                if (onChainMetadata != null) {
                    // Optionally save to database for future lookups
                    try {
                        val entityToSave = onChainMetadata.copy(
                            swappable = true,
                            groupId = "custom_${normalizedAddress}"
                        )
                        tokenMetadataDao.upsertTokensMetadata(listOf(entityToSave))
                    } catch (e: Exception) {
                        Log.w("AlchemyTokenMetadataRepository", "Error saving custom token to database", e)
                    }

                    return@withContext TokenMetadata(
                        contractAddress = onChainMetadata.contractAddress,
                        chainId = onChainMetadata.chainId,
                        symbol = onChainMetadata.symbol,
                        name = onChainMetadata.name,
                        decimals = onChainMetadata.decimals,
                        logo = onChainMetadata.logo,
                        swappable = true
                    )
                }
            } catch (e: Exception) {
                Log.e("AlchemyTokenMetadataRepository", "Error fetching on-chain metadata", e)
            }

            return@withContext null
        }
    }
}