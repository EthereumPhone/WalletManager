package com.core.domain

import com.core.data.repository.WalletRepository

class GetWalletAddressUseCase(
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): String = walletRepository.getAddress()
}



