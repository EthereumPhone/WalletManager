package com.core.database

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.core.database.util.TokenSeedingHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Database callbacks for handling token seeding
 */
object DatabaseCallbacks {
    
    private const val TAG = "DatabaseCallbacks"
    private const val PREF_NAME = "wm_database_prefs"
    private const val KEY_TOKENS_SEEDED = "tokens_seeded_v4"
    
    /**
     * Creates a callback that seeds tokens on database creation or migration.
     * This uses a context-based approach to handle the seeding properly.
     */
    fun createTokenSeedingCallback(context: Context): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "Database created, seeding tokens...")
                
                // Seed tokens immediately on creation
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Clear the seeding flag to ensure we seed fresh
                        resetSeedingFlag(context)
                        
                        TokenSeedingHelper.seedTokensDirectly(context, db)
                        markSeedingComplete(context)
                        Log.d(TAG, "Token seeding completed successfully on creation")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error seeding tokens on creation", e)
                    }
                }
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                
                // Check if we need to seed tokens (for migrations or incomplete seeding)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        
                        // Check if we've already seeded tokens
                        if (!prefs.getBoolean(KEY_TOKENS_SEEDED, false)) {
                            Log.d(TAG, "Tokens not seeded, starting token seeding...")
                            
                            TokenSeedingHelper.seedTokensDirectly(context, db)
                            markSeedingComplete(context)
                            
                            Log.d(TAG, "Token seeding completed successfully")
                        } else {
                            Log.d(TAG, "Tokens already seeded, skipping")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error seeding tokens", e)
                        // Don't crash the app if seeding fails
                    }
                }
            }
        }
    }
    
    /**
     * Marks token seeding as complete
     */
    private fun markSeedingComplete(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_TOKENS_SEEDED, true).apply()
    }
    
    /**
     * Resets the seeding flag (useful for testing)
     */
    fun resetSeedingFlag(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TOKENS_SEEDED).apply()
    }
}