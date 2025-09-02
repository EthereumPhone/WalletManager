package com.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


internal object DatabaseMigrations {
    
    /**
     * Migration from version 2 to 3:
     * - Adds token_group table for grouping tokens across chains
     * - Adds token_bridge table for tracking bridge relationships
     * - Adds groupId column to token_metadata table
     * - Creates necessary indices for performance
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("""
                CREATE TABLE IF NOT EXISTS token_group (
                    groupId TEXT PRIMARY KEY NOT NULL,
                    canonicalChainId INTEGER NOT NULL,
                    canonicalAddress TEXT NOT NULL,
                    symbol TEXT NOT NULL,
                    name TEXT NOT NULL
                )
            """)

            database.execSQL("""
                CREATE TABLE IF NOT EXISTS token_bridge (
                    sourceChainId INTEGER NOT NULL,
                    sourceAddress TEXT NOT NULL,
                    targetChainId INTEGER NOT NULL,
                    targetAddress TEXT NOT NULL,
                    groupId TEXT NOT NULL,
                    PRIMARY KEY(sourceChainId, sourceAddress, targetChainId, targetAddress)
                )
            """)
            
            // 3. Add groupId column to token_metadata table
            // Note: SQLite doesn't support adding foreign key constraints to existing tables,
            // so we add the column without the constraint
            database.execSQL("""
                ALTER TABLE token_metadata 
                ADD COLUMN groupId TEXT DEFAULT NULL
            """)
            
            // 4. Create indices for performance
            database.execSQL("CREATE INDEX IF NOT EXISTS index_token_metadata_groupId ON token_metadata(groupId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_token_bridge_groupId ON token_bridge(groupId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_token_bridge_source ON token_bridge(sourceChainId, sourceAddress)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_token_bridge_target ON token_bridge(targetChainId, targetAddress)")
            
            // 5. Create unique index for token_metadata to prevent duplicates
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_token_metadata_chain_address ON token_metadata(chainId, contractAddress)")

        }
    }
    
    /**
     * Migration from version 3 to 4:
     * - No schema changes, but marks database as having pre-seeded tokens
     * - This migration is specifically for handling the transition from
     *   runtime seeding to pre-populated database
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // No schema changes needed
            // The version bump itself indicates that token seeding should be handled
            // This allows us to differentiate between:
            // - New installations (will get pre-populated database at version 4)
            // - Existing installations (will migrate through this and keep their data)
            
            // You could optionally check if tokens exist and seed them here if needed
            // For example:
            val cursor = database.query("SELECT COUNT(*) FROM token_metadata WHERE groupId IS NOT NULL")
            cursor.use {
                if (it.moveToFirst() && it.getInt(0) == 0) {
                    // No grouped tokens exist, this installation needs seeding
                    // Mark for seeding via a metadata table or handle it here
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS database_metadata (
                            key TEXT PRIMARY KEY NOT NULL,
                            value TEXT
                        )
                    """)
                    database.execSQL("""
                        INSERT OR REPLACE INTO database_metadata (key, value) 
                        VALUES ('needs_token_seeding', 'true')
                    """)
                }
            }
        }
    }
    
    /**
     * All migrations for the database.
     * Add new migrations here as the schema evolves.
     */
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_2_3,
        MIGRATION_3_4
        // Future migrations will be added here: MIGRATION_4_5, etc.
    )
    
    /**
     * Helper function to get migrations for a specific version range.
     * Useful for testing specific migration paths.
     */
    fun getMigrations(startVersion: Int, endVersion: Int): Array<Migration> {
        return ALL_MIGRATIONS.filter { migration ->
            migration.startVersion >= startVersion && migration.endVersion <= endVersion
        }.toTypedArray()
    }
}