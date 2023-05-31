package org.ethereumphone.walletmanager.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ethereumphone.walletmanager.core.domain.model.NetworkWalletData


@Entity(tableName = "network_information")
data class NetworkInformationEntity(
    @PrimaryKey
    val chainId: Int,
    val address: String,
    val networkBalance: Double,
)

fun NetworkInformationEntity.asExternalModule() = NetworkWalletData(
    chainId = chainId,
    address = address,
    networkBalance = networkBalance,
)