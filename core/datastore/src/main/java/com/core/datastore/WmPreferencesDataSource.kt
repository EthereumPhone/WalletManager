package com.core.datastore

import androidx.datastore.core.DataStore
import com.core.datastore.proto.UserPreferences
import com.core.datastore.proto.copy
import com.core.model.UserData
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WmPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>
) {
    val userData = userPreferences.data
        .map {
            UserData(
                walletAddress = it.walletAddress
            )
        }

    suspend fun setWalletAddress(address: String) {
        userPreferences.updateData {
            it.copy {
                this.walletAddress = address
            }
        }
    }
}