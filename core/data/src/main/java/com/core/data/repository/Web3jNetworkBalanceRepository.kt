package com.core.data.repository

import com.core.data.remote.NetworkBalanceApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenBalance
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
    override fun getNetworksBalance(): Flow<List<TokenBalance>> =
        tokenBalanceDao.getTokenBalances(NetworkChain.getAllNetworkChains()
            .map { it.chainId.toString() }
        ).map { it.map(TokenBalanceEntity::asExternalModule) }

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