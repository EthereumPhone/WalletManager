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
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenBalance
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
    // Distinct native gas-token tickers across all supported chains (ETH, MATIC, BNB, AVAX, MON, APE).
    private val nativeSymbols: List<String> =
        NetworkChain.getAllNetworkChains().map { it.nativeSymbol }.distinct()

    private fun List<TokenExchangeEntity>.latestUsdRateBySymbol(): Map<String, Double> =
        groupBy { it.symbol }
            .mapValues { (_, rows) -> rows.maxByOrNull { it.timestamp }?.value ?: 0.0 }

    override fun getNetworkTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() })
            .map { items ->
                items.map { tb ->
                    val net = NetworkChain.getNetworkByChainId(tb.chainId)
                    TokenAsset(
                        address = tb.contractAddress,
                        chainId = tb.chainId,
                        symbol = net?.nativeSymbol ?: "",
                        name = net?.nativeName ?: "",
                        balance = formatSmallBalance(tb.tokenBalance.toDouble()),
                        decimals = net?.nativeDecimals ?: 18
                    )
                }
            }

    override fun getGroupedNetworkTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() })
            .map { items ->
                // Group native balances by their gas-token ticker (ETH across all its chains,
                // MATIC, BNB, AVAX, MON, APE...), summing each into a single asset.
                items.groupBy { NetworkChain.getNetworkByChainId(it.chainId)?.nativeSymbol ?: "ETH" }
                    .map { (symbol, assets) ->
                        val canonical = NetworkChain.getAllNetworkChains()
                            .filter { it.nativeSymbol == symbol }.minByOrNull { it.chainId }
                        val sum = assets.sumOf { it.tokenBalance }
                        TokenAsset(
                            address = "network_${symbol.lowercase()}",
                            chainId = canonical?.chainId ?: assets.first().chainId,
                            symbol = symbol,
                            name = symbol,
                            balance = formatSmallBalance(sum.toDouble()),
                            logoUrl = symbol,
                            decimals = canonical?.nativeDecimals ?: 18
                        )
                    }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getGroupedNetworkTokensOverview(): Flow<List<TokenGroupAssetOverview>> =
        // Combine balances with the latest USD rate for every native ticker so the flow
        // re-emits when prices change.
        combine(
            tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() }),
            tokenExchangeDao.observeExchangesBySymbols(nativeSymbols, "usd")
        ) { items, exchanges ->
            val rates = exchanges.latestUsdRateBySymbol()

            items.groupBy { NetworkChain.getNetworkByChainId(it.chainId)?.nativeSymbol ?: "ETH" }
                .mapNotNull { (symbol, assets) ->
                    if (assets.isEmpty()) return@mapNotNull null

                    val totalBalance = assets.sumOf { it.tokenBalance }.toDouble()
                    val exchangeRate = rates[symbol] ?: 0.0
                    val totalFiatBalance = if (exchangeRate > 0) totalBalance * exchangeRate else 0.0

                    TokenGroupAssetOverview(
                        groupId = "network_${symbol.lowercase()}",
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

    override fun getNetworkTokensWithExchange(): Flow<List<TokenAssetWithPrice>> =
        // Return individual network tokens per chain with exchange rates
        combine(
            tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() }),
            tokenExchangeDao.observeExchangesBySymbols(nativeSymbols, "usd")
        ) { items, exchanges ->
            val rates = exchanges.latestUsdRateBySymbol()
            items.mapNotNull { tokenBalance ->
                val net = NetworkChain.getNetworkByChainId(tokenBalance.chainId) ?: return@mapNotNull null
                val balance = tokenBalance.tokenBalance.toDouble()

                // Skip zero balances
                if (balance == 0.0) return@mapNotNull null

                val exchangeRate = rates[net.nativeSymbol] ?: 0.0
                val fiatAmount = if (exchangeRate > 0) balance * exchangeRate else 0.0

                TokenAssetWithPrice(
                    // Use chainId as address for native tokens (matches existing convention)
                    address = tokenBalance.chainId.toString(),
                    chainId = tokenBalance.chainId,
                    symbol = net.nativeSymbol,
                    name = net.nativeName,
                    balance = balance,
                    decimals = net.nativeDecimals,
                    logoUrl = net.nativeSymbol,
                    swappable = true,
                    fiatAmount = fiatAmount
                )
            }
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
            
            // One group per distinct native currency (ETH spans all its chains; MATIC, BNB,
            // AVAX, MON, APE...), with one metadata row per chain pointing to its native group.
            networks.groupBy { it.nativeSymbol }.forEach { (symbol, chainsForSymbol) ->
                val canonical = chainsForSymbol.minByOrNull { it.chainId } ?: return@forEach
                tokenGroups.add(
                    TokenGroupEntity(
                        groupId = canonical.nativeGroupId,
                        canonicalChainId = canonical.chainId,
                        canonicalAddress = canonical.chainId.toString(),
                        symbol = symbol,
                        name = canonical.nativeName
                    )
                )
                chainsForSymbol.forEach { network ->
                    tokenMetadata.add(
                        TokenMetadataEntity(
                            contractAddress = network.chainId.toString(),
                            chainId = network.chainId,
                            decimals = network.nativeDecimals,
                            name = network.nativeName,
                            symbol = symbol,
                            logo = symbol,
                            swappable = true,
                            groupId = canonical.nativeGroupId
                        )
                    )
                }
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
        val network = NetworkChain.getNetworkByChainId(chainId) ?: return

        withContext(Dispatchers.IO) {
            // Create token group and metadata for this chain's native (gas) token.
            val symbol = network.nativeSymbol
            val canonical = NetworkChain.getAllNetworkChains()
                .filter { it.nativeSymbol == symbol }.minByOrNull { it.chainId } ?: network

            val tokenGroup = TokenGroupEntity(
                groupId = network.nativeGroupId,
                canonicalChainId = canonical.chainId,
                canonicalAddress = canonical.chainId.toString(),
                symbol = symbol,
                name = network.nativeName
            )

            val tokenMetadata = TokenMetadataEntity(
                contractAddress = chainId.toString(),
                chainId = chainId,
                decimals = network.nativeDecimals,
                name = network.nativeName,
                symbol = symbol,
                logo = symbol,
                swappable = true,
                groupId = network.nativeGroupId
            )
            
            // Insert token group and metadata
            tokenGroupDao.upsertTokenGroup(tokenGroup)
            tokenMetadataDao.upsertTokensMetadata(listOf(tokenMetadata))
            
            // Update balance
            async {
                try {
                    val newNetworkBalance = networkBalanceApi
                        .getNetworkCurrency(
                            toAddress,
                            "https://${network.chainName}.g.alchemy.com/v2/${chainToApiKey(network.chainName)}"
                        )
                    tokenBalanceDao.upsertTokenBalances(
                        listOf(
                            TokenBalanceEntity(
                                contractAddress = network.chainId.toString(),
                                chainId = network.chainId,
                                tokenBalance = newNetworkBalance
                            )
                        )
                    )
                } catch (e: Exception) {
                    Log.e("Web3jNetworkBalanceRepository", "Failed to fetch balance for chain $chainId: ${e.message}")
                }
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