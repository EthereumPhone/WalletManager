package com.core.data.repository

import com.core.database.model.TransferEntity
import com.core.model.Transfer
import kotlinx.coroutines.flow.Flow

interface TransferRepository {
    fun getTransfers(): Flow<List<Transfer>>
    fun getTransfers(chainId: Int): Flow<List<Transfer>>
    fun getTransfers(categories: List<String>): Flow<List<Transfer>>
    fun getTransfers(
        chainId: Int,
        categories: List<String>
    ): Flow<List<Transfer>>

    suspend fun refreshTransfers(address: String)


    suspend fun insertTransfer(transfer: TransferEntity)
    suspend fun deleteTransfer(
        chainId: Int,
        value: Double,
        ispending: Boolean,
        userIsSender: Boolean,
        toaddress: String
    )
}