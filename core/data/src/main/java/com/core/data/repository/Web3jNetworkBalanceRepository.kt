package com.core.data.repository

import android.util.Log
import com.core.data.remote.NetworkBalanceApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenExchangeDao
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.CompositeToken
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenBalance
import com.core.model.TokenAsset
import com.core.model.TokenGroupAssetOverview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * I intend to reuse the Token Balance entity for the Network currency,
 * to distinguish them from the erc20 tokens, The address of the token will the same as the chainID,
 * For instance; USDC-mainnet address: "<some hex sequence>", mainnet eth address = "1"
 */
class Web3jNetworkBalanceRepository @Inject constructor(
    private val networkBalanceApi: NetworkBalanceApi,
    private val tokenBalanceDao: TokenBalanceDao,
    private val tokenGroupDao: TokenGroupDao,
    private val tokenMetadataDao: TokenMetadataDao,
    private val tokenExchangeRepository: DefaultExchangeRepository,
    private val tokenExchangeDao: TokenExchangeDao
): NetworkBalanceRepository {
    override fun getNetworkTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() })
            .map { items ->
                items.map {
                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                    TokenAsset(
                        address = it.contractAddress,
                        chainId = it.chainId,
                        symbol = name,
                        name = name,
                        balance = formatSmallBalance(it.tokenBalance.toDouble()),
                        decimals = 18
                    )
                }
            }

    override fun getGroupedNetworkTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() })
            .map { items ->
                val grouped = items.groupBy { it.chainId == 137 }

                grouped.map { (isPolygon, assets) ->
                    val name = if(isPolygon) "MATIC" else "ETH"
                    val sum = assets.sumOf { it.tokenBalance}

                    TokenAsset(
                        address = if (isPolygon) "network_matic" else "network_eth",
                        chainId = if (isPolygon) 137 else 1,
                        symbol = name,
                        name = name,
                        balance = formatSmallBalance(sum.toDouble()),
                        logoUrl = if (isPolygon) "MATIC" else "ETH",
                        decimals = 18
                    )
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getGroupedNetworkTokensOverview(): Flow<List<TokenGroupAssetOverview>> =
        // Combine token balances with exchange rate changes to ensure proper Flow updates
        combine(
            tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() }),
            tokenExchangeDao.getExchangeBySymbolFlow("ETH", "usd"),
            tokenExchangeDao.getExchangeBySymbolFlow("MATIC", "usd")
        ) { items, ethExchange, maticExchange ->
            val grouped = items.groupBy { it.chainId == 137 }
            
            grouped.mapNotNull { (isPolygon, assets) ->
                if (assets.isEmpty()) return@mapNotNull null
                
                val symbol = if(isPolygon) "MATIC" else "ETH"
                val sum = assets.sumOf { it.tokenBalance }
                val totalBalance = sum.toDouble()

                val exchangeEntity = if (isPolygon) maticExchange else ethExchange
                val exchangeRate = exchangeEntity?.value ?: 0.0

                // Calculate fiat balance
                val totalFiatBalance = if (exchangeRate > 0) totalBalance * exchangeRate else 0.0

                TokenGroupAssetOverview(
                    groupId = if (isPolygon) "network_matic" else "network_eth",
                    symbol = symbol,
                    name = symbol,
                    totalBalance = totalBalance,
                    formattedBalance = formatSmallBalance(totalBalance).toString(),
                    logoUrl = symbol,
                    totalFiatBalance = if (exchangeRate > 0) totalFiatBalance else null,
                    formattedFiatBalance = if (exchangeRate > 0) String.format("%.2f", totalFiatBalance) else null,
                    exchangeCurrency = "usd"
                )
            }.filter { it.totalBalance != 0.0 }
        }


    override fun getNetworkBalance(chainId: Int): Flow<TokenBalance> =
        tokenBalanceDao.getTokenBalances(listOf(chainId.toString()))
            .map { it.first().asExternalModule() }

    override suspend fun refreshNetworkBalance(
        toAddress: String,
        chainIds: List<Int>
    ) {


        val networks = chainIds.mapNotNull {
            NetworkChain.getNetworkByChainId(it)
        }

        withContext(Dispatchers.IO) {
            // First, create all token groups and metadata
            val tokenGroups = mutableListOf<TokenGroupEntity>()
            val tokenMetadata = mutableListOf<TokenMetadataEntity>()
            
            // Group ETH and MATIC separately
            val ethNetworks = networks.filter { it.chainId != 137 }
            val maticNetwork = networks.find { it.chainId == 137 }
            
            // Create ETH group if we have any ETH networks
            if (ethNetworks.isNotEmpty()) {
                tokenGroups.add(
                    TokenGroupEntity(
                        groupId = "network_eth",
                        canonicalChainId = 1,
                        canonicalAddress = "1",
                        symbol = "ETH",
                        name = "ETH"
                    )
                )
                
                // Create metadata for each ETH network
                ethNetworks.forEach { network ->
                    tokenMetadata.add(
                        TokenMetadataEntity(
                            contractAddress = network.chainId.toString(),
                            chainId = network.chainId,
                            decimals = 18,
                            name = "ETH",
                            symbol = "ETH",
                            logo = "ETH",
                            swappable = true,
                            groupId = "network_eth"
                        )
                    )
                }
            }
            
            // Create MATIC group if we have MATIC network
            if (maticNetwork != null) {
                tokenGroups.add(
                    TokenGroupEntity(
                        groupId = "network_matic",
                        canonicalChainId = 137,
                        canonicalAddress = "137",
                        symbol = "MATIC",
                        name = "MATIC"
                    )
                )
                
                tokenMetadata.add(
                    TokenMetadataEntity(
                        contractAddress = "137",
                        chainId = 137,
                        decimals = 18,
                        name = "MATIC",
                        symbol = "MATIC",
                        logo = "MATIC",
                        swappable = true,
                        groupId = "network_matic"
                    )
                )
            }
            
            // Insert token groups and metadata before updating balances
            if (tokenGroups.isNotEmpty()) {
                tokenGroupDao.upsertTokenGroups(tokenGroups)
                tokenMetadataDao.upsertTokensMetadata(tokenMetadata)
            }

            // Then update balances
            networks.map {
                if(it.chainId != 7777777) {
                    async {
                        try {
                            val newNetworkBalance = networkBalanceApi
                                .getNetworkCurrency(
                                    toAddress,
                                    "https://${it.chainName}.g.alchemy.com/v2/${chainToApiKey(it.chainName)}"
                                )
                            
                            tokenBalanceDao.upsertTokenBalances(
                                listOf(
                                    TokenBalanceEntity(
                                        contractAddress = it.chainId.toString(),
                                        chainId = it.chainId,
                                        tokenBalance = newNetworkBalance
                                    )
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("Web3jNetworkBalanceRepository", "Failed to fetch balance for chain ${it.chainId}: ${e.message}")
                            // Don't crash - just skip this network's balance update
                        }
                    }
                } else {
                    async {
                        var zoraFetcher: Web3j? = null
                        try {
                            zoraFetcher = Web3j.build(HttpService("https://rpc.zora.energy"))
                            val amount = zoraFetcher.ethGetBalance(toAddress, DefaultBlockParameterName.LATEST)
                                .sendAsync().get()

                            tokenBalanceDao.upsertTokenBalances(
                                listOf(
                                    TokenBalanceEntity(
                                        contractAddress = it.chainId.toString(),
                                        chainId = it.chainId,
                                        tokenBalance = Convert.fromWei(amount.balance.toString(), Convert.Unit.ETHER)
                                    )
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("Web3jNetworkBalanceRepository", "Failed to fetch Zora balance: ${e.message}")
                            // Don't crash - Zora RPC may be blocked in some regions (e.g., China)
                        } finally {
                            zoraFetcher?.shutdown()
                        }
                    }
                }
            }
        }
    }

    override suspend fun refreshNetworkBalanceByNetwork(toAddress: String, chainId: Int) {
        val network = NetworkChain.getNetworkByChainId(chainId)

        withContext(Dispatchers.IO) {
            // Create token group and metadata for network token
            val isPolygon = chainId == 137
            val symbol = if (isPolygon) "MATIC" else "ETH"
            val groupId = if (isPolygon) "network_matic" else "network_eth"
            
            val tokenGroup = TokenGroupEntity(
                groupId = groupId,
                canonicalChainId = if (isPolygon) 137 else 1,
                canonicalAddress = if (isPolygon) "137" else "1",
                symbol = symbol,
                name = symbol
            )
            
            val tokenMetadata = TokenMetadataEntity(
                contractAddress = chainId.toString(),
                chainId = chainId,
                decimals = 18,
                name = symbol,
                symbol = symbol,
                logo = symbol,
                swappable = true,
                groupId = groupId
            )
            
            // Insert token group and metadata
            tokenGroupDao.upsertTokenGroup(tokenGroup)
            tokenMetadataDao.upsertTokensMetadata(listOf(tokenMetadata))
            
            // Update balance
            async {
                val newNetworkBalance = networkBalanceApi
                    .getNetworkCurrency(
                        toAddress,
                        "https://${network!!.chainName}.g.alchemy.com/v2/${chainToApiKey(network!!.chainName)}"
                    )
                tokenBalanceDao.upsertTokenBalances(
                    listOf(
                        TokenBalanceEntity(
                            contractAddress = network!!.chainId.toString(),
                            chainId = network!!.chainId,
                            tokenBalance = newNetworkBalance
                        )
                    )
                )
            }


        }
    }
}

fun formatSmallBalance(balance: Double): Double {
    if (balance == 0.0) return 0.0

    val precision = 6
    val minDisplayableValue = 1.0 / Math.pow(10.0, precision.toDouble())

    // For very small values (less than minDisplayableValue), return the minimum displayable value
    if (balance > 0 && balance < minDisplayableValue) {
        return minDisplayableValue
    }

    // Otherwise, round to 6 decimal places
    val bd = BigDecimal(balance)
    val rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP)
    return rounded.toDouble()
}