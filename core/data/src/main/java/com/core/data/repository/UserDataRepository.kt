package com.core.data.repository

import com.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    val userData: Flow<UserData>
    suspend fun setWalletAddress(address: String)
    suspend fun setWalletNetwork(network: String)
}