package com.core.data.repository


import android.util.Log
import com.core.data.model.dto.TokenBalanceDto
import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenBalanceRequestBody
import com.core.data.remote.TokenBalanceApi
import com.core.data.service.OnChainTokenMetadataFetcher
import com.core.data.util.chainToApiKey
import com.core.data.util.spamTokens
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.CompositeToken
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.asExternalModule
import com.core.database.model.erc20.toExternalModel
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.ethereumphone.walletsdk.WalletSDK
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.collections.flatten

class AlchemyTokenBalanceRepository @Inject constructor(
    private val tokenBalanceApi: TokenBalanceApi,
    private val tokenBalanceDao: TokenBalanceDao,
    private val tokenMetadataDao: TokenMetadataDao,
    private val tokenGroupDao: TokenGroupDao,
    private val onChainTokenMetadataFetcher: OnChainTokenMetadataFetcher
): TokenBalanceRepository {
    override fun getTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getCompositeTokens().map {
            it.map(CompositeToken::toExternalModel)
        }

    override fun observeBalancesWithoutMetadata(): Flow<List<TokenBalanceEntity>> =
        tokenBalanceDao.observeTokenBalancesWithoutMetadataFlow()

    override fun getCombinedTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getCompositeTokens().map { allCompositeTokens ->
            // Group tokens: those with metadata by symbol, those without separately
            val tokensWithMetadata = allCompositeTokens.filter { it.tokenMetadataEntity != null }
            val tokensWithoutMetadata = allCompositeTokens.filter { it.tokenMetadataEntity == null && it.tokenBalanceEntity != null }
            
            // Process tokens with metadata (group by symbol)
            val groupedBySymbol = tokensWithMetadata.groupBy { it.tokenMetadataEntity!!.symbol }
            
            val assetsWithMetadata = groupedBySymbol.mapNotNull { (symbol, assetsWithSameSymbol) ->
                val representativeToken = assetsWithSameSymbol
                    .firstOrNull { it.tokenBalanceEntity != null }
                    ?: return@mapNotNull null

                val totalBalanceForSymbol = assetsWithSameSymbol.sumOf { compositeToken ->
                    val balanceEntity = compositeToken.tokenBalanceEntity
                    val metadataEntity = compositeToken.tokenMetadataEntity
                    if (balanceEntity != null && metadataEntity != null) {
                        balanceEntity.tokenBalance.movePointLeft(metadataEntity.decimals)
                    } else {
                        BigDecimal.ZERO
                    }
                }

                representativeToken.tokenMetadataEntity?.let { metadata ->
                    TokenAsset(
                        address = metadata.contractAddress,
                        chainId = metadata.chainId,
                        symbol = symbol,
                        name = metadata.name,
                        balance = totalBalanceForSymbol.stripTrailingZeros().toDouble(),
                        decimals = metadata.decimals,
                        logoUrl = metadata.logo,
                        swappable = metadata.swappable
                    )
                }
            }
            
            // Process tokens without metadata (use contract address as identifier)
            val assetsWithoutMetadata = tokensWithoutMetadata.map { compositeToken ->
                val balanceEntity = compositeToken.tokenBalanceEntity!!
                // Use default decimals of 18 for tokens without metadata
                val decimals = 18
                val balance = balanceEntity.tokenBalance
                    .movePointLeft(decimals)
                    .stripTrailingZeros()
                    .toDouble()
                
                // Use shortened address as symbol/name for now
                val shortAddress = "${balanceEntity.contractAddress.take(6)}...${balanceEntity.contractAddress.takeLast(4)}"
                
                TokenAsset(
                    address = balanceEntity.contractAddress,
                    chainId = balanceEntity.chainId,
                    symbol = shortAddress,
                    name = "Unknown Token",
                    balance = balance,
                    decimals = decimals,
                    logoUrl = null,
                    swappable = false
                )
            }
            
            // Combine both lists
            assetsWithMetadata + assetsWithoutMetadata
        }

    override fun getTokensBalances(): Flow<List<TokenBalance>> =
        tokenBalanceDao.getTokenBalances()
            .map { it.map(TokenBalanceEntity::asExternalModule) }

    override fun getTokensBalances(contractAddresses: List<String>): Flow<List<TokenBalance>> =
        tokenBalanceDao.getTokenBalances(contractAddresses)
            .map { it.map(TokenBalanceEntity::asExternalModule) }

    override fun getTokensBalances(chainId: Int): Flow<List<TokenBalance>> =
        tokenBalanceDao.getTokenBalances(chainId)
            .map { it.map(TokenBalanceEntity::asExternalModule) }

    override suspend fun refreshTokensBalances(toAddress: String) {
        Log.d("TonkenBalance API", "update started")


        val requestBody = TokenBalanceRequestBody.allErc20Tokens(toAddress)
        val spam = spamTokens.asSequence().map { it.lowercase() }.toSet()
        val networks = NetworkChain.getAllNetworkChains()

        supervisorScope {
            val allEntities = networks
                .map { network ->
                    async {
                        val apiKey = chainToApiKey(network.chainName)
                        val url = "https://${network.chainName}.g.alchemy.com/v2/$apiKey"

                        try {
                            tokenBalanceApi
                                .getTokenBalances(url, requestBody)
                                .result.tokenBalances
                                .asSequence()
                                .filter { it.contractAddress.lowercase() !in spam }
                                .map { it.asEntity(network.chainId) }
                                .toList()

                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList()
                        }
                    }
                }
                .awaitAll()
                .flatten()

            if (allEntities.isNotEmpty()) {
                tokenBalanceDao.upsertTokenBalances(allEntities)
                
                // Fetch metadata for tokens that don't have it
                fetchMissingMetadataOnChain()
            }
        }
    }

    override suspend fun refreshTokensBalancesByNetwork(toAddress: String, chainId: Int) {
        val network = NetworkChain.getNetworkByChainId(chainId)


        withContext(Dispatchers.IO) {
            val apiKey = chainToApiKey(network!!.chainName)
            async {
                val results = tokenBalanceApi.getTokenBalances(
                    "https://${network!!.chainName}.g.alchemy.com/v2/$apiKey",
                    TokenBalanceRequestBody.allErc20Tokens(toAddress)
                ).result.tokenBalances
                    .filter { it.contractAddress !in spamTokens }
                    .map { it.asEntity(network!!.chainId) }
                tokenBalanceDao.upsertTokenBalances(results)
                
                // Fetch metadata for tokens that don't have it
                fetchMissingMetadataOnChain()
            }
        }
    }
    
    /**
     * Fetches metadata on-chain for tokens that don't have metadata in the database
     */
    private suspend fun fetchMissingMetadataOnChain() {
        withContext(Dispatchers.IO) {
            try {
                // Get all token balances without metadata
                val tokensWithoutMetadata = tokenBalanceDao.observeTokenBalancesWithoutMetadataFlow()
                    .first() // Get the current value
                
                if (tokensWithoutMetadata.isEmpty()) {
                    Log.d("AlchemyTokenBalanceRepository", "No tokens without metadata found")
                    return@withContext
                }
                
                Log.d("AlchemyTokenBalanceRepository", "Found ${tokensWithoutMetadata.size} tokens without metadata")
                
                // Group tokens by chain ID for efficient processing
                val tokensByChain = tokensWithoutMetadata.groupBy { it.chainId }
                
                supervisorScope {
                    tokensByChain.map { (chainId, tokens) ->
                        async {
                            val network = NetworkChain.getNetworkByChainId(chainId)
                            if (network != null) {
                                val apiKey = chainToApiKey(network.chainName)
                                val rpcUrl = "https://${network.chainName}.g.alchemy.com/v2/$apiKey"
                                
                                // Fetch metadata for each token on this chain
                                val tokenGroups = mutableListOf<TokenGroupEntity>()
                                val metadataList = tokens.mapNotNull { token ->
                                    Log.d("AlchemyTokenBalanceRepository", "Fetching on-chain metadata for ${token.contractAddress} on chain $chainId")
                                    val metadata = onChainTokenMetadataFetcher.fetchTokenMetadata(
                                        contractAddress = token.contractAddress,
                                        chainId = chainId,
                                        rpcUrl = rpcUrl
                                    )
                                    
                                    // If metadata was fetched, create a group for it
                                    if (metadata != null) {
                                        val resolvedGroupId = resolveGroupId(
                                            chainId = chainId,
                                            address = token.contractAddress,
                                            symbol = metadata.symbol
                                        )
                                        
                                        val existingGroup = tokenGroupDao.getGroupedToken(resolvedGroupId)
                                        if (existingGroup == null) {
                                            val tokenGroup = TokenGroupEntity(
                                                groupId = resolvedGroupId,
                                                canonicalChainId = chainId,
                                                canonicalAddress = token.contractAddress.lowercase(),
                                                symbol = metadata.symbol,
                                                name = metadata.name
                                            )
                                            tokenGroups.add(tokenGroup)
                                        }
                                        
                                        // Return metadata with resolved groupId
                                        metadata.copy(groupId = resolvedGroupId)
                                    } else {
                                        null
                                    }
                                }
                                
                                // Store the token groups first (due to foreign key constraints)
                                if (tokenGroups.isNotEmpty()) {
                                    Log.d("AlchemyTokenBalanceRepository", "Storing ${tokenGroups.size} token groups")
                                    tokenGroupDao.upsertTokenGroups(tokenGroups)
                                }
                                
                                // Then store the fetched metadata
                                if (metadataList.isNotEmpty()) {
                                    Log.d("AlchemyTokenBalanceRepository", "Storing ${metadataList.size} fetched metadata entries")
                                    tokenMetadataDao.upsertTokensMetadata(metadataList)
                                }
                            }
                        }
                    }.awaitAll()
                }
            } catch (e: Exception) {
                Log.e("AlchemyTokenBalanceRepository", "Error fetching missing metadata on-chain", e)
            }
        }
    }

    private suspend fun resolveGroupId(chainId: Int, address: String, symbol: String): String {
        val byBridge = tokenGroupDao.findGroupIdByBridge(chainId, address)
        if (byBridge != null) return byBridge
        val bySymbol = tokenGroupDao.findGroupIdBySymbolPreferMainnet(symbol)
        if (bySymbol != null) return bySymbol
        return "${chainId}_${address.lowercase()}"
    }

}