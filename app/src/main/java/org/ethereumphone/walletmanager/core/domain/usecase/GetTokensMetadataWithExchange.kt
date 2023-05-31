package org.ethereumphone.walletmanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.ethereumphone.walletmanager.core.domain.model.ERCToken
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata
import org.ethereumphone.walletmanager.core.domain.repository.TokenExchangeRepository
import org.ethereumphone.walletmanager.core.domain.repository.TokenMetadataRepository
import org.ethereumphone.walletmanager.core.util.Resource
import javax.inject.Inject

class GetTokensMetadataWithExchange @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenExchangeRepository: TokenExchangeRepository
) {
    operator fun invoke(): Flow<Resource<List<ERCToken>>> =
        combine(
            tokenMetadataRepository.getTokens(),
            tokenExchangeRepository.getAllTokenExchanges()
        ) { metadatas, exchanges ->
            mapToERCTokens(metadatas = metadatas, exchanges = exchanges)
        }
}

private fun mapToERCTokens(
    metadatas: Resource<List<TokenMetadata>>,
    exchanges: Resource<List<TokenExchange>>
): Resource<List<ERCToken>> {
    val metadataList = metadatas.data ?: emptyList()
    val exchangeList = exchanges.data ?: emptyList()
    val tokens = metadataList.map { metadata ->
        ERCToken(
            exchange = exchangeList.find { it.symbol == metadata.symbol } ?: TokenExchange(symbol = metadata.symbol, price = 0.0),
            metadata = metadata
        )
    }
    return when {
        metadatas is Resource.Success && exchanges is Resource.Success -> Resource.Success(data = tokens)
        metadatas is Resource.Error && exchanges is Resource.Error -> Resource.Error(data = tokens, message = exchanges.message ?: "")
        else -> Resource.Loading(tokens)
    }
}