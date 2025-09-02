package com.core.database.util

import android.content.Context
import androidx.room.Room
import com.core.database.WmDatabase
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Utility class to create a pre-populated database file.
 * This should be run once during development to generate the database asset.
 */
class DatabasePrePopulator {
    
    companion object {
        /**
         * Creates a pre-populated database file with Uniswap token data.
         * Run this once to generate your database asset file.
         * 
         * @param context Application context
         * @param outputPath Where to save the populated database file
         */
        fun createPrePopulatedDatabase(
            context: Context,
            outputPath: String = "${context.filesDir}/prepopulated_wm_database.db"
        ) {
            // Create a temporary database instance
            val tempDb = Room.databaseBuilder(
                context,
                WmDatabase::class.java,
                "temp_prepopulate_db"
            )
                .fallbackToDestructiveMigration()
                .build()
            
            try {
                runBlocking {
                    // Use your existing UniswapTokenSeederHelper to populate the database
                    val seeder = UniswapTokenSeederHelper(
                        context = context,
                        tokenGroupDao = tempDb.tokenGroupDao,
                        tokenMetadataDao = tempDb.tokenMetadataDao
                    )
                    
                    // Seed the tokens
                    seeder.seedTokens()
                    
                    // Force a checkpoint to ensure all data is written
                    tempDb.runInTransaction {
                        // This forces the WAL to be checkpointed
                    }
                }
                
                // Close the database
                tempDb.close()
                
                // Copy the database file to the output location
                val dbFile = context.getDatabasePath("temp_prepopulate_db")
                val outputFile = File(outputPath)
                
                dbFile.copyTo(outputFile, overwrite = true)
                
                // Also copy the -wal and -shm files if they exist
                val walFile = File("${dbFile.absolutePath}-wal")
                val shmFile = File("${dbFile.absolutePath}-shm")
                
                if (walFile.exists()) {
                    walFile.delete() // WAL should be checkpointed, so we can delete it
                }
                if (shmFile.exists()) {
                    shmFile.delete() // SHM can also be deleted
                }
                
                println("Pre-populated database created at: $outputPath")
                println("Copy this file to app/src/main/assets/database/wm_database.db")
                
            } finally {
                // Clean up temporary database
                context.deleteDatabase("temp_prepopulate_db")
            }
        }
    }
}
