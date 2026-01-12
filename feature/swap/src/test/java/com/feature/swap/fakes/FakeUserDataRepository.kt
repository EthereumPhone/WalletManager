package com.feature.swap.fakes

import com.core.data.repository.UserDataRepository
import com.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserDataRepository(
    initialUserData: UserData = UserData(
        walletAddress = "",
        walletNetwork = "",
        isFirstBoot = true,
        preferredCurrency = "usd"
    )
) : UserDataRepository {

    private val state = MutableStateFlow(initialUserData)

    override val userData: Flow<UserData> = state.asStateFlow()

    override suspend fun setWalletAddress(address: String) {
        state.value = state.value.copy(walletAddress = address)
    }

    override suspend fun setWalletNetwork(network: String) {
        state.value = state.value.copy(walletNetwork = network)
    }

    override suspend fun setIsFirstBoot(isFirstBoot: Boolean) {
        state.value = state.value.copy(isFirstBoot = isFirstBoot)
    }

    override suspend fun setPreferredCurrency(currency: String) {
        state.value = state.value.copy(preferredCurrency = currency)
    }

    fun emit(userData: UserData) {
        state.value = userData
    }
}



