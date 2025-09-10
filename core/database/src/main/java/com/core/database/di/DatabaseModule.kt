package com.core.database.di

import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
     * Clears all datastore files to ensure fresh start after database migration
     */
    private fun clearDatastoreFiles(context: Context) {
        try {
            // Clear user preferences datastore
            val userPrefsFile = File(context.filesDir, "datastore/user_preferences.pb")
            if (userPrefsFile.exists()) {
                userPrefsFile.delete()
                Log.d("DatabaseModule", "Cleared user preferences datastore")
            }
            
            // Clear exclusion list datastore
            val exclusionListFile = File(context.filesDir, "datastore/exclusion_list.pb.pb")
            if (exclusionListFile.exists()) {
                exclusionListFile.delete()
                Log.d("DatabaseModule", "Cleared exclusion list datastore")
            }
            
            // Clear any other datastore-related files in the datastore directory
            val datastoreDir = File(context.filesDir, "datastore")
            if (datastoreDir.exists() && datastoreDir.isDirectory) {
                datastoreDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".pb") || file.name.endsWith(".tmp")) {
                        file.delete()
                        Log.d("DatabaseModule", "Cleared datastore file: ${file.name}")
                    }
                }
            }
            
            // Also clear the database-related SharedPreferences
            val prefs = context.getSharedPreferences("wm_database_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d("DatabaseModule", "Cleared database SharedPreferences")
            
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Error clearing datastore files", e)
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
                
                // If database is at version 2-5, delete it to force recreation
                if (currentVersion in 2..5) {
                    Log.w("DatabaseModule", "Database at version $currentVersion, deleting for clean migration to version 6")
                    context.deleteDatabase("wm-database")
                    // Also clear datastore to ensure fresh start
                    clearDatastoreFiles(context)
                }
            } catch (e: Exception) {
                Log.e("DatabaseModule", "Error checking database version, will attempt normal migration", e)
            }
        }
        
        return try {
            // Build the database
            buildDatabase(context, moshi)
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Failed to build database, deleting and recreating", e)
            // If any error occurs, delete the database and recreate it
            context.deleteDatabase("wm-database")
            // Also clear datastore to ensure fresh start
            clearDatastoreFiles(context)
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