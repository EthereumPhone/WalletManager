package com.feature.paymaster.fakes

import com.core.data.repository.WalletRepository

class FakeWalletRepository(
    private var address: String = ""
) : WalletRepository {
    override fun getAddress(): String = address
    fun setAddress(newAddress: String) {
        address = newAddress
    }
}



