package com.example.transactions.fakes

import com.core.data.repository.TransferRepository
import com.core.model.Transfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTransferRepository(
    initialTransfers: List<Transfer> = emptyList()
) : TransferRepository {

    private val transfersState = MutableStateFlow(initialTransfers)
    private val hasTransfersState = MutableStateFlow(initialTransfers.isNotEmpty())

    var lastRefreshedAddress: String? = null
        private set

    override fun getTransfers(): Flow<List<Transfer>> = transfersState.asStateFlow()

    override fun getTransfers(chainId: Int): Flow<List<Transfer>> = transfersState.asStateFlow()

    override fun getTransfers(categories: List<String>): Flow<List<Transfer>> =
        transfersState.asStateFlow()

    override fun getTransfers(
        chainId: Int,
        categories: List<String>
    ): Flow<List<Transfer>> = transfersState.asStateFlow()

    override fun observeTransfersExist(): Flow<Boolean> = hasTransfersState.asStateFlow()

    override suspend fun refreshTransfers(address: String) {
        lastRefreshedAddress = address
    }

    fun emitTransfers(transfers: List<Transfer>) {
        transfersState.value = transfers
        hasTransfersState.value = transfers.isNotEmpty()
    }
}





