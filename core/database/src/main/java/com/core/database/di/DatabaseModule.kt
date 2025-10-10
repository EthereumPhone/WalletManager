package com.core.database.di

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import androidx.room.Room
import com.core.database.DatabaseCallbacks
import com.core.database.WmDatabase
import com.core.database.util.BigDecimalTypeConverter
import com.core.database.util.Erc1155MetadataConverter
import com.core.database.util.MoshiJsonConverter
import com.core.database.util.RawContractConverter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Gets the current version of the database by querying the room_master_table
     */
    private fun getCurrentDatabaseVersion(context: Context): Int {
        val dbPath = context.getDatabasePath("wm-database").absolutePath
        val db = SQLiteDatabase.openDatabase(
            dbPath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        
        return db.use {
            val cursor = it.rawQuery("PRAGMA user_version", null)
            cursor.use { c ->
                if (c.moveToFirst()) {
                    c.getInt(0)
                } else {
                    0
                }
            }
        }
    }
    
    /**
     * Clears all app data to ensure completely fresh start after database migration
     */
    private fun clearAllAppData(context: Context) {
        try {
            Log.w("DatabaseModule", "Starting complete app data clear for fresh migration")
            
            // 1. Clear all SharedPreferences
            val prefsDir = File(context.filesDir.parent, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".xml")) {
                        // Clear the preferences content first
                        val prefName = file.name.removeSuffix(".xml")
                        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                            .edit().clear().apply()
                        Log.d("DatabaseModule", "Cleared SharedPreferences: $prefName")
                    }
                }
            }
            
            // 2. Clear all databases except the one we're migrating
            val databasesDir = File(context.filesDir.parent, "databases")
            if (databasesDir.exists() && databasesDir.isDirectory) {
                databasesDir.listFiles()?.forEach { file ->
                    if (file.name != "wm-database" && !file.name.startsWith("wm-database-")) {
                        file.delete()
                        Log.d("DatabaseModule", "Deleted database: ${file.name}")
                    }
                }
            }
            
            // 3. Clear all files in internal storage
            clearDirectory(context.filesDir, "internal files")
            
            // 4. Clear all cache
            clearDirectory(context.cacheDir, "cache")
            clearDirectory(context.codeCacheDir, "code cache")
            
            // 5. Clear external cache if available
            context.externalCacheDir?.let { clearDirectory(it, "external cache") }
            
            // 6. Clear no backup files directory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                clearDirectory(context.noBackupFilesDir, "no backup files")
            }
            
            Log.w("DatabaseModule", "Completed app data clear - app will behave as fresh install")
            
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Error clearing app data", e)
        }
    }
    
    /**
     * Recursively clears all files in a directory
     */
    private fun clearDirectory(dir: File?, description: String) {
        if (dir == null || !dir.exists()) return
        
        try {
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        clearDirectory(file, "$description/${file.name}")
                    }
                    file.delete()
                }
                Log.d("DatabaseModule", "Cleared $description directory")
            }
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Error clearing $description", e)
        }
    }

    @Provides
    @Singleton
    fun provideWmDatabase(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): WmDatabase {
        // Check if database exists and needs migration
        val dbFile = context.getDatabasePath("wm-database")
        if (dbFile.exists()) {
            try {
                // Check the current database version
                val currentVersion = getCurrentDatabaseVersion(context)
                Log.d("DatabaseModule", "Current database version: $currentVersion")
                
                // If database is at version 2-6, perform complete data clear
                if (currentVersion in 2..6) {
                    Log.w("DatabaseModule", "Database at version $currentVersion, performing complete data clear for fresh migration to version 7")
                    
                    // Special handling for version 2 -> 7 migration
                    if (currentVersion == 2) {
                        Log.w("DatabaseModule", "Detected migration from version 2 to 7 - clearing all app storage and cache")
                    }
                    
                    // Special handling for version 6 -> 7: TokenBalanceEntity now uses composite primary key
                    if (currentVersion == 6) {
                        Log.w("DatabaseModule", "Detected migration from version 6 to 7 - TokenBalanceEntity schema changed to composite primary key, clearing all app data")
                    }
                    
                    // Clear all app data for a completely fresh start
                    clearAllAppData(context)
                    // Then delete the database
                    context.deleteDatabase("wm-database")
                }
            } catch (e: Exception) {
                Log.e("DatabaseModule", "Error checking database version, will attempt normal migration", e)
            }
        }
        
        return try {
            // Build the database
            buildDatabase(context, moshi)
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Failed to build database, performing complete data clear and recreating", e)
            // If any error occurs, clear all app data for fresh start
            clearAllAppData(context)
            // Delete the database
            context.deleteDatabase("wm-database")
            buildDatabase(context, moshi)
        }
    }
    
    private fun buildDatabase(
        context: Context,
        moshi: Moshi
    ): WmDatabase {
        val builder = Room.databaseBuilder(
            context,
            WmDatabase::class.java,
            "wm-database"
        )
            .addTypeConverter(Erc1155MetadataConverter(MoshiJsonConverter(moshi)))
            .addTypeConverter(RawContractConverter(MoshiJsonConverter(moshi)))
            .addTypeConverter(BigDecimalTypeConverter())
            
        // Don't add any migrations - we want destructive migration for all versions
        // if (withMigrations) {
        //     builder.addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        // }
        
        return builder
            // Add callback for token seeding on database creation/open
            .addCallback(DatabaseCallbacks.createTokenSeedingCallback(context))
            // Enable destructive migration as fallback for all versions
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

}