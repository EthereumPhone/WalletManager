package org.ethereumphone.walletmanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ethereumphone.walletmanager.core.database.dao.TokenMetadataDao
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata
import org.ethereumphone.walletmanager.core.domain.repository.TokenMetadataRepository
import org.ethereumphone.walletmanager.core.util.Resource
import javax.inject.Inject

class TokenMetadataRepositoryImplementation @Inject constructor(
    private val dao: TokenMetadataDao
): TokenMetadataRepository {

    override fun getTokens(): Flow<Resource<List<TokenMetadata>>> = flow {
        emit(Resource.Loading())
        val result = dao.getTokensMetadata()
        emit(Resource.Success(result))

    }

    override fun insertAllToken(tokens: List<TokenMetadata>) {
        dao.insertAllTokens(tokens)
    }

    override fun getTokenMetadataBySymbol(symbol: String): Flow<Resource<TokenMetadata>> = flow {
        emit(Resource.Loading())
        val result = dao.getTokenMetadataBySymbol(symbol)
        emit(Resource.Success(result))

    }

    override fun getTokenMetadataByDecimals(decimals: Int): Flow<Resource<TokenMetadata>> = flow {
        emit(Resource.Loading())
        val result = dao.getTokenMetadataByDecimals(decimals)
        emit(Resource.Success(result))
    }

    override fun getTokenMetadataByName(name: String): Flow<Resource<TokenMetadata>> = flow {
        emit(Resource.Loading())
        val result = dao.getTokenMetadataByName(name)
        emit(Resource.Success(result))
    }
}