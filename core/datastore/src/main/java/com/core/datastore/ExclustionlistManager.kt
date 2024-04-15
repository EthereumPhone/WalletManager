package com.core.datastore

import androidx.datastore.core.DataStore
import com.core.datastore.proto.ExclusionListProto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExclusionListManager @Inject constructor(
    private val dataStore: DataStore<ExclusionListProto.ExclusionList>
) {

    // Get the current exclusion list
    val exclusionList: Flow<List<String>> = dataStore.data
        .map { exclusionList ->
            exclusionList.excludedIdsList
        }

    // Add an item to the exclusion list
    suspend fun addToExclusionList(itemId: String) {
        dataStore.updateData { currentList ->
            currentList.toBuilder()
                .addExcludedIds(itemId)
                .build()
        }
    }

    // Remove an item from the exclusion list
    suspend fun removeFromExclusionList(itemId: String) {
        dataStore.updateData { currentList ->
            val modifiedList = currentList.excludedIdsList.toMutableList()
            modifiedList.remove(itemId)
            currentList.toBuilder()
                .clearExcludedIds()
                .addAllExcludedIds(modifiedList)
                .build()
        }
    }
}