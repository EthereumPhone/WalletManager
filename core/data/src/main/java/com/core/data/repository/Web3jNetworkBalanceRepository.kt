package com.core.data.repository

import android.util.Log
import com.core.data.remote.NetworkBalanceApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.CompositeToken
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenBalance
import com.core.model.TokenAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
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
    private val tokenBalanceDao: TokenBalanceDao
): NetworkBalanceRepository {
    override fun getNetworkTokens(): Flow<List<TokenAsset>> =
        tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains().map { it.chainId.toString() })
            .map { items ->
                items.map {
                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                    TokenAsset(
                        address = it.contractAddress,
                        chainId = it.chainId,
                        symbol = name.lowercase(),
                        name = name.lowercase(),
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
                        address = if (isPolygon) "137" else "1",
                        chainId = if (isPolygon) 137 else 1,
                        symbol = name.lowercase(),
                        name = name.lowercase(),
                        balance = formatSmallBalance(sum.toDouble()),
                        logoUrl = if (isPolygon) "MATIC" else "ETH",
                        decimals = 18
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

            networks.map {
                if(it.chainId != 7777777) {
                    async {
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
                    }
                } else {

                    val zoraFetcher = Web3j.build(HttpService("https://rpc.zora.energy"))
                    val amount = zoraFetcher.ethGetBalance(toAddress, DefaultBlockParameterName.LATEST
                    ).sendAsync().get()

                    tokenBalanceDao.upsertTokenBalances(
                        listOf(
                            TokenBalanceEntity(
                                contractAddress = it.chainId.toString(),
                                chainId = it.chainId,
                                tokenBalance = Convert.fromWei(amount.balance.toString(), Convert.Unit.ETHER)
                            )
                        )
                    )
                }

            }
        }
    }

    override suspend fun refreshNetworkBalanceByNetwork(toAddress: String, chainId: Int) {
        val network = NetworkChain.getNetworkByChainId(chainId)

        withContext(Dispatchers.IO) {
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