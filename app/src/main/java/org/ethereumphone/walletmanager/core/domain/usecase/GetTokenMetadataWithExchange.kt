package org.ethereumphone.walletmanager.core.domain.usecase

import org.ethereumphone.walletmanager.core.domain.repository.TokenExchangeRepository
import org.ethereumphone.walletmanager.core.domain.repository.TokenMetadataRepository
import javax.inject.Inject

class GetTokenMetadataWithExchange @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenExchangeRepository: TokenExchangeRepository
) {


}
