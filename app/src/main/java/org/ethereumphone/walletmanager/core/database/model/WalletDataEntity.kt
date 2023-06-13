package org.ethereumphone.walletmanager.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ethereumphone.walletmanager.core.domain.model.WalletData
@Entity(tableName = "network_information")
data class WalletDataEntity(
    @PrimaryKey
    val chainId: Int,
    val name: String, // alchemy api network name
    val rpc: String,
    val address: String,
    val networkBalance: Double
) {
    fun toNetworkWalletData(): WalletData {
        return WalletData(
            chainId = chainId,
            name = name,
            rpc = rpc,
            address = address,
            networkBalance = networkBalance
        )
    }
}
