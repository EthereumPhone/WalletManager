package com.core.database.repository

import com.core.database.dao.TokenGroupDao
import com.core.database.model.erc20.CompositeTokenGroup
import com.core.database.model.erc20.TokenGroupAsset
import com.core.database.model.erc20.toExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for accessing grouped token data.
 * Provides a clean API for the UI layer to consume grouped tokens.
 */
@Singleton
class TokenGroupRepository @Inject constructor(
    private val tokenGroupDao: TokenGroupDao
) {
    
    /**
     * Get all token groups with their aggregated balances.
     * Returns a flow that emits whenever token balances change.
     */
    fun getAllTokenGroups(): Flow<List<TokenGroupAsset>> {
        return tokenGroupDao.getAllCompositeTokenGroups()
            .map { groups ->
                groups.map { it.toExternalModel() }
            }
    }
    
    /**
     * Get only token groups that have a non-zero balance.
     * Useful for displaying the user's actual holdings.
     */
    fun getActiveTokenGroups(): Flow<List<TokenGroupAsset>> {
        return tokenGroupDao.getActiveCompositeTokenGroups()
            .map { groups ->
                groups
                    .filter { it.hasBalance }
                    .sortedByDescending { it.totalBalance }
                    .map { it.toExternalModel() }
            }
    }
    
    /**
     * Get a specific token group by ID.
     */
    suspend fun getTokenGroup(groupId: String): TokenGroupAsset? {
        return tokenGroupDao.getCompositeTokenGroup(groupId)?.toExternalModel()
    }
    
    /**
     * Get token groups sorted by total balance.
     */
    suspend fun getTokenGroupsSortedByBalance(): List<TokenGroupAsset> {
        return tokenGroupDao.getAllCompositeTokenGroupsSync()
            .sortedByDescending { it.totalBalance }
            .map { it.toExternalModel() }
    }
    
    /**
     * Search token groups by symbol or name.
     */
    suspend fun searchTokenGroups(query: String): List<TokenGroupAsset> {
        val lowerQuery = query.lowercase()
        return tokenGroupDao.getAllCompositeTokenGroupsSync()
            .filter { group ->
                group.tokenGroup.symbol.lowercase().contains(lowerQuery) ||
                group.tokenGroup.name.lowercase().contains(lowerQuery)
            }
            .sortedByDescending { it.totalBalance }
            .map { it.toExternalModel() }
    }
    
}

