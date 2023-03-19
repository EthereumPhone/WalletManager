package org.ethereumphone.walletmanager.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "network_information")
data class NetworkInformationEntity(
    @PrimaryKey
    val chainID: Int,
    val chainName: String,
    val networkBalance: Float,
    val fiatBalance: Float
)

fun NetworkInformationEntity.asExternalModule() = NetworkInformationEntity(
    chainID = chainID,
    chainName = chainName,
    networkBalance = networkBalance,
    fiatBalance = fiatBalance
)