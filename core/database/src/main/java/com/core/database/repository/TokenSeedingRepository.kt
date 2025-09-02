package com.core.database.repository

import android.content.Context
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.util.UniswapTokenSeederHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to handle token seeding for existing installations.
 * This is only needed for users upgrading from version 3 to 4.
 */
@Singleton
class TokenSeedingRepository @Inject constructor(
    private val context: Context,
    private val tokenGroupDao: TokenGroupDao,
    private val tokenMetadataDao: TokenMetadataDao
) {
    companion object {
        private const val PREF_NAME = "token_seeding_prefs"
        private const val KEY_SEEDING_COMPLETE = "seeding_complete_v4"
    }
    
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Checks if token seeding is needed for this installation
     */
    suspend fun isSeedingNeeded(): Boolean = withContext(Dispatchers.IO) {
        // If already marked as complete, no need to seed
        if (prefs.getBoolean(KEY_SEEDING_COMPLETE, false)) {
            return@withContext false
        }
        
        // Check if we have any tokens with group IDs (indicates seeding was done)
        val hasGroupedTokens = tokenMetadataDao.getTokenCount() > 0
        
        if (hasGroupedTokens) {
            // Mark as complete if we already have tokens
            markSeedingComplete()
            return@withContext false
        }
        
        true
    }
    
    /**
     * Seeds tokens if needed for existing installations
     */
    suspend fun seedTokensIfNeeded(): Flow<SeedingStatus> = flow {
        if (!isSeedingNeeded()) {
            emit(SeedingStatus.NotNeeded)
            return@flow
        }
        
        emit(SeedingStatus.InProgress)
        
        try {
            val seeder = UniswapTokenSeederHelper(
                context = context,
                tokenGroupDao = tokenGroupDao,
                tokenMetadataDao = tokenMetadataDao
            )
            
            seeder.seedTokens()
            markSeedingComplete()
            emit(SeedingStatus.Complete)
            
        } catch (e: Exception) {
            emit(SeedingStatus.Error(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Marks token seeding as complete
     */
    private fun markSeedingComplete() {
        prefs.edit().putBoolean(KEY_SEEDING_COMPLETE, true).apply()
    }
    
    /**
     * Resets the seeding flag (useful for testing)
     */
    fun resetSeedingFlag() {
        prefs.edit().remove(KEY_SEEDING_COMPLETE).apply()
    }
}

/**
 * Status of token seeding operation
 */
sealed class SeedingStatus {
    object NotNeeded : SeedingStatus()
    object InProgress : SeedingStatus()
    object Complete : SeedingStatus()
    data class Error(val message: String) : SeedingStatus()
}
