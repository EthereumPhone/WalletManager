package org.ethereumphone.walletmanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange
import org.ethereumphone.walletmanager.core.util.Resource

interface TokenExchangeRepository {
    fun getTokenExchange(symbol: String): Flow<Resource<TokenExchange>>
    fun getAllTokenExchanges(): Flow<Resource<List<TokenExchange>>>
}