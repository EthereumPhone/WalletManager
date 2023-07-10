package com.core.data.repository


import com.core.data.model.dto.asEntity
import com.core.data.model.requestBody.TokenBalanceRequestBody
import com.core.data.remote.TokenBalanceApi
import com.core.data.util.chainToApiKey
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.asExternalModule
import com.core.model.NetworkChain
import com.core.model.TokenBalance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import javax.inject.Inject

class AlchemyTokenBalanceRepository @Inject constructor(
    private val tokenBalanceApi: TokenBalanceApi,
    private val tokenBalanceDao: TokenBalanceDao,
): TokenBalanceRepository {
    override fun getTokensBalances(): Flow<List<TokenBalance>> =
        tokenBalanceDao.getTokenBalances()
            .map { it.map(TokenBalanceEntity::asExternalModule) }

    override fun getTokensBalances(
        contractAddresses: List<String>
    ): Flow<List<TokenBalance>> =
        tokenBalanceDao.getTokenBalances(contractAddresses)
            .map { it.map(TokenBalanceEntity::asExternalModule) }

    override suspend fun refreshTokensBalances(toAddress: String) {
        withContext(Dispatchers.IO) {
            val networks = NetworkChain.getAllNetworkChains()
            networks.map { network ->
                val apiKey = chainToApiKey(network.chainName)
                async {
                    val results = tokenBalanceApi.getTokenBalances(
                        "https://${network.chainName}.g.alchemy.com/v2/$apiKey",
                        TokenBalanceRequestBody.allErc20Tokens(toAddress)
                    ).map { it.asEntity(network.chainId) }

                    val test = results.wait()
                    tokenBalanceDao.upsertTokenBalances(results)
                }
            }
        }
    }
}