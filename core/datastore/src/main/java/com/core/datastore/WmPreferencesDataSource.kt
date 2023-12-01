package com.core.datastore

import androidx.datastore.core.DataStore
import com.core.datastore.proto.UserPreferences
import com.core.datastore.proto.copy
import com.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WmPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>
) {
    val userData: Flow<UserData> = userPreferences.data
        .map {
            UserData(
                walletAddress = it.walletAddress,
                walletNetwork = it.walletNetwork,
                onboardingCompleted = it.onboardingCompleted,
                preferredCurrency = it.preferredCurrency
            )
        }

    suspend fun setWalletAddress(address: String) {
        userPreferences.updateData {
            it.copy {
                this.walletAddress = address
            }
        }
    }
    suspend fun setWalletNetwork(network: String) {
        userPreferences.updateData {
            it.copy {
                this.walletNetwork = network
            }
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.onboardingCompleted = completed
            }
        }
    }

    suspend fun setPreferredCurrency(currency: String) {
        userPreferences.updateData {
            it.copy {
                this.preferredCurrency = currency
            }
        }
    }
}