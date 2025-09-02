package com.core.database.util

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.core.database.WmDatabase
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Standalone script to generate a pre-populated database.
 * This should be run once during development to create the database asset file.
 * 
 * Usage:
 * 1. Run this as an instrumented test or create a temporary Activity
 * 2. The generated database will be saved to the device's files directory
 * 3. Pull the database file using adb:
 *    adb pull /data/data/your.package.name/files/prepopulated_wm.db
 * 4. Place the database in app/src/main/assets/database/wm_database.db
 */
class GeneratePrePopulatedDatabase {
    
    companion object {
        
        /**
         * Generates a pre-populated Room database with all Uniswap tokens.
         * This creates a clean database at version 4 with all tokens pre-loaded.
         */
        @JvmStatic
        fun generate(context: Context) {
            runBlocking {
                println("Starting database pre-population...")
                
                // Create a fresh database at version 4
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    WmDatabase::class.java,
                    "temp_prepopulated.db"
                )
                    .fallbackToDestructiveMigration() // Start fresh
                    .build()
                
                try {
                    // Seed the database with Uniswap tokens
                    val seeder = UniswapTokenSeederHelper(
                        context = context,
                        tokenGroupDao = database.tokenGroupDao,
                        tokenMetadataDao = database.tokenMetadataDao
                    )
                    
                    println("Seeding tokens...")
                    seeder.seedTokens()
                    println("Token seeding complete!")
                    
                    // Close the database to ensure all data is written
                    database.close()
                    
                    // Copy the database file to a known location
                    val sourceDb = context.getDatabasePath("temp_prepopulated.db")
                    val destFile = File(context.filesDir, "prepopulated_wm.db")
                    
                    // Ensure WAL mode is checkpointed
                    val walFile = File("${sourceDb.absolutePath}-wal")
                    val shmFile = File("${sourceDb.absolutePath}-shm")
                    
                    // Copy main database file
                    sourceDb.copyTo(destFile, overwrite = true)
                    
                    // Clean up WAL files if they exist
                    if (walFile.exists()) walFile.delete()
                    if (shmFile.exists()) shmFile.delete()
                    
                    println("✅ Pre-populated database created successfully!")
                    println("📍 Location: ${destFile.absolutePath}")
                    println("")
                    println("Next steps:")
                    println("1. Pull the database from device:")
                    println("   adb pull ${destFile.absolutePath}")
                    println("2. Create directory: app/src/main/assets/database/")
                    println("3. Copy the database to: app/src/main/assets/database/wm_database.db")
                    println("")
                    println("Database info:")
                    println("- Size: ${destFile.length() / 1024} KB")
                    println("- Version: 4")
                    
                } catch (e: Exception) {
                    println("❌ Error generating pre-populated database: ${e.message}")
                    e.printStackTrace()
                } finally {
                    // Clean up temporary database
                    context.deleteDatabase("temp_prepopulated.db")
                }
            }
        }
    }
}
