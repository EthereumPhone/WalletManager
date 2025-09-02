package com.core.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Test database migrations to ensure data integrity during schema changes.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    
    companion object {
        private const val TEST_DB = "migration-test"
    }
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WmDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        // Create database with version 2
        helper.createDatabase(TEST_DB, 2).apply {
            // Insert test data for version 2
            execSQL("""
                INSERT INTO token_metadata (contractAddress, decimals, name, symbol, logo, chainId, swappable)
                VALUES 
                ('0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2', 18, 'Wrapped Ether', 'WETH', 'logo.png', 1, 1),
                ('0x4200000000000000000000000000000000000006', 18, 'Wrapped Ether', 'WETH', 'logo.png', 10, 1)
            """)
            
            close()
        }
        
        // Run migration and validate schema
        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, DatabaseMigrations.MIGRATION_2_3)
        
        // Verify new tables exist and can be queried
        db.query("SELECT * FROM token_group").use { cursor ->
            assert(cursor != null) { "token_group table should exist" }
        }
        
        db.query("SELECT * FROM token_bridge").use { cursor ->
            assert(cursor != null) { "token_bridge table should exist" }
        }
        
        // Verify groupId column was added to token_metadata
        db.query("SELECT groupId FROM token_metadata").use { cursor ->
            assert(cursor != null) { "groupId column should exist in token_metadata" }
        }
        
        // Verify indices exist
        db.query("SELECT * FROM sqlite_master WHERE type='index' AND name='index_token_metadata_groupId'").use { cursor ->
            assert(cursor.moveToFirst()) { "index_token_metadata_groupId should exist" }
        }
        
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create oldest version of database
        helper.createDatabase(TEST_DB, 2).apply {
            close()
        }
        
        // Open latest version to trigger all migrations
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            WmDatabase::class.java,
            TEST_DB
        ).addMigrations(*DatabaseMigrations.ALL_MIGRATIONS).build().apply {
            openHelper.writableDatabase.close()
        }
    }
}
