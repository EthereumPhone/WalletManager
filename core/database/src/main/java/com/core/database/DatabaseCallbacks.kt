package com.core.database

import android.content.Context
import android.content.SharedPreferences
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.util.UniswapTokenSeederHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Database callbacks for handling token seeding in existing installations
 */
object DatabaseCallbacks {
    
    private const val PREF_NAME = "wm_database_prefs"
    private const val KEY_TOKENS_SEEDED = "tokens_seeded_v1"
    
    /**
     * Callback that seeds Uniswap tokens for existing installations.
     * This runs only once when the database is opened for the first time after the update.
     */
    val TOKEN_SEEDING_CALLBACK = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            
            // This will need to be injected properly in your actual implementation
            // For now, showing the pattern
            CoroutineScope(Dispatchers.IO).launch {
                seedTokensIfNeeded(db)
            }
        }
    }
    
    /**
     * Seeds tokens if they haven't been seeded yet for existing installations
     */
    private suspend fun seedTokensIfNeeded(db: SupportSQLiteDatabase) {
        // Note: In a real implementation, you'll need to properly inject these dependencies
        // This is a simplified example showing the pattern
        
        // You would need to access the context and DAOs here
        // Typically through dependency injection
        
        // Check if we've already seeded the tokens
        // val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // if (!prefs.getBoolean(KEY_TOKENS_SEEDED, false)) {
        //     val seeder = UniswapTokenSeederHelper(
        //         context = context,
        //         tokenGroupDao = tokenGroupDao,
        //         tokenMetadataDao = tokenMetadataDao
        //     )
        //     
        //     seeder.seedTokens()
        //     
        //     // Mark as seeded
        //     prefs.edit().putBoolean(KEY_TOKENS_SEEDED, true).apply()
        // }
    }
    
    /**
     * Alternative approach using a provider pattern for dependency injection
     */
    class CallbackProvider(
        private val context: Context,
        private val tokenGroupDao: TokenGroupDao,
        private val tokenMetadataDao: TokenMetadataDao
    ) {
        fun createCallback(): RoomDatabase.Callback {
            return object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    
                    CoroutineScope(Dispatchers.IO).launch {
                        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        if (!prefs.getBoolean(KEY_TOKENS_SEEDED, false)) {
                            val seeder = UniswapTokenSeederHelper(
                                context = context,
                                tokenGroupDao = tokenGroupDao,
                                tokenMetadataDao = tokenMetadataDao
                            )
                            
                            seeder.seedTokens()
                            
                            // Mark as seeded
                            prefs.edit().putBoolean(KEY_TOKENS_SEEDED, true).apply()
                        }
                    }
                }
            }
        }
    }
}
