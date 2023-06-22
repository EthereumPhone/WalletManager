package com.core.data.repository

import com.core.Resource.Resource
import com.core.model.EntryCategory
import com.core.model.Transfer
import kotlinx.coroutines.flow.Flow

interface TransferRepository {
    fun getAllNetworksTransfers(fetchRemote: Boolean): Flow<Resource<List<Transfer>>>
    fun getAllTransfersByChainId(
        fetchRemote: Boolean,
        chainId: Int
    ): Flow<Resource<List<Transfer>>>
    fun getAllTransfersByCategories(
        fetchRemote: Boolean,
        categories: List<EntryCategory>): Flow<Resource<List<Transfer>>>
    fun getAllTransfersByChainAndCategories(
        fetchRemote: Boolean,
        chainId: Int,
        categories: List<EntryCategory>): Flow<Resource<List<Transfer>>>
}