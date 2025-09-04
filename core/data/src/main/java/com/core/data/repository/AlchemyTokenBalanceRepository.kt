package com.core.data.repository


import android.util.Log
import com.core.data.model.dto.TokenBalanceDto
import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenBalanceRequestBody
import com.core.data.remote.TokenBalanceApi
import com.core.data.util.chainToApiKey
import com.core.data.util.spamTokens
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.CompositeToken
import com.core.database.model.erc20.TokenBalanceEntity
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
): TokenBalanceRepository {
    override fun getTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getCompositeTokens().map {
            it.map(CompositeToken::toExternalModel)
        }

    override fun observeBalancesWithoutMetadata(): Flow<List<TokenBalanceEntity>> =
        tokenBalanceDao.observeTokenBalancesWithoutMetadataFlow()

    override fun getCombinedTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getCompositeTokens().map { allCompositeTokens ->
            val groupedBySymbol = allCompositeTokens.groupBy { it.tokenMetadataEntity.symbol }

            groupedBySymbol.mapNotNull { (symbol, assetsWithSameSymbol) ->
                val representativeToken = assetsWithSameSymbol
                    .firstOrNull { it.tokenBalanceEntity != null }
                    ?: return@mapNotNull null

                val totalBalanceForSymbol = assetsWithSameSymbol.sumOf { compositeToken ->
                    val balanceEntity = compositeToken.tokenBalanceEntity
                    val metadataEntity = compositeToken.tokenMetadataEntity
                    if (balanceEntity != null) {
                        balanceEntity.tokenBalance.movePointLeft(metadataEntity.decimals)
                    } else {
                        BigDecimal.ZERO
                    }
                }

                TokenAsset(
                    address = representativeToken.tokenMetadataEntity.contractAddress,
                    chainId = representativeToken.tokenMetadataEntity.chainId,
                    symbol = symbol,
                    name = representativeToken.tokenMetadataEntity.name,
                    balance = totalBalanceForSymbol.stripTrailingZeros().toDouble(),
                    decimals = representativeToken.tokenMetadataEntity.decimals,
                    logoUrl = representativeToken.tokenMetadataEntity.logo,
                    swappable = representativeToken.tokenMetadataEntity.swappable
                )
            }
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
            }
        }
    }

}