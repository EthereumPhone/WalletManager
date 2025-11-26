package com.feature.home.fakes

import com.core.data.repository.TransferRepository
import com.core.model.Transfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTransferRepository(
    initialTransfers: List<Transfer> = emptyList(),
    initialHasTransfers: Boolean = false
) : TransferRepository {

    private val transfersState = MutableStateFlow(initialTransfers)
    private val hasTransfersState = MutableStateFlow(initialHasTransfers)

    override fun getTransfers(): Flow<List<Transfer>> = transfersState

    override fun getTransfers(chainId: Int): Flow<List<Transfer>> = transfersState

    override fun getTransfers(categories: List<String>): Flow<List<Transfer>> = transfersState

    override fun getTransfers(chainId: Int, categories: List<String>): Flow<List<Transfer>> = transfersState

    override fun observeTransfersExist(): Flow<Boolean> = hasTransfersState

    override suspend fun refreshTransfers(address: String) {
        // no-op for tests
    }

    fun emitTransfers(transfers: List<Transfer>) {
        transfersState.value = transfers
        hasTransfersState.value = transfers.isNotEmpty()
    }

    fun setHasTransfers(value: Boolean) {
        hasTransfersState.value = value
    }
}


