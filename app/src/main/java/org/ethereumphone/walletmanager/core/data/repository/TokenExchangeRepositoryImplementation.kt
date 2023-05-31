package org.ethereumphone.walletmanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ethereumphone.walletmanager.core.data.remote.ExchangeApi
import org.ethereumphone.walletmanager.core.database.dao.TokenExchangeDao
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange
import org.ethereumphone.walletmanager.core.domain.repository.TokenExchangeRepository
import org.ethereumphone.walletmanager.core.util.Resource
import retrofit2.HttpException
import javax.inject.Inject

class TokenExchangeRepositoryImplementation @Inject constructor(
    private val api: ExchangeApi,
    private val dao: TokenExchangeDao
): TokenExchangeRepository
{
    override fun getTokenExchange(symbol: String): Flow<Resource<TokenExchange>> = flow {
        emit(Resource.Loading())

        val tokenExchange = dao.getTokenExchange(symbol = symbol)
        emit(Resource.Loading(data = tokenExchange))

        try {
            val remoteTokenExchange = api.getExchange(symbol)
            dao.upsertTokenExchange(remoteTokenExchange)
        } catch (e: HttpException) {
            emit(
                Resource.Error(
                "request couldn't be finalized",
                data = tokenExchange
            ))
        }

        val newTokenExchange = dao.getTokenExchange(symbol = symbol)
        emit(Resource.Success(newTokenExchange))
    }

    override fun getAllTokenExchanges(): Flow<Resource<List<TokenExchange>>> = flow {
        emit(Resource.Loading())

        val tokenExchanges = dao.getAllTokenExchanges()
        emit(Resource.Loading(tokenExchanges))

        try {
            val remoteTokenExchanges = tokenExchanges.map { api.getExchange(it.symbol) }
            remoteTokenExchanges.forEach{ dao.upsertTokenExchange(it) }
        } catch (e: HttpException) {
            emit(
                Resource.Error(
                "request couldn't be finalized",
                data = tokenExchanges
            ))
        }
        val newTokenExchange = dao.getAllTokenExchanges()
        emit(Resource.Success(newTokenExchange))
    }
}