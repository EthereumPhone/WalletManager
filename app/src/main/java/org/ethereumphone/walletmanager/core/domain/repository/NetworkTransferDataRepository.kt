package org.ethereumphone.walletmanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumphone.walletmanager.core.database.dao.EntryCategory
import org.ethereumphone.walletmanager.core.domain.model.Transfer
import org.ethereumphone.walletmanager.core.util.Resource


interface NetworkTransferDataRepository {
    fun getAllNetworksTransfers(): Flow<Resource<List<Transfer>>>
    fun getAllTransfersByChainId(chainId: Int): Flow<Resource<List<Transfer>>>
    fun getAllTransfersByCategories(categories: List<EntryCategory>): Flow<Resource<List<Transfer>>>
    fun getAllTransfersByChainAndCategories(chainId: Int, categories: List<EntryCategory>): Flow<Resource<List<Transfer>>>
}