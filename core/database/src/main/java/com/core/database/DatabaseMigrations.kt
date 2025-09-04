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
     * Migration from version 4 to 5:
     * - Changes primary key of token_metadata from contractAddress to composite (contractAddress, chainId)
     * - This fixes the issue where tokens with same address on different chains were conflicting
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Create new table with correct primary key structure
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS token_metadata_new (
                    contractAddress TEXT NOT NULL,
                    chainId INTEGER NOT NULL,
                    decimals INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    symbol TEXT NOT NULL,
                    logo TEXT,
                    swappable INTEGER NOT NULL DEFAULT 0,
                    groupId TEXT,
                    PRIMARY KEY(contractAddress, chainId),
                    FOREIGN KEY(groupId) REFERENCES token_group(groupId) ON DELETE SET NULL
                )
            """)
            
            // 2. Create index on the new table
            database.execSQL("CREATE INDEX IF NOT EXISTS index_token_metadata_new_groupId ON token_metadata_new(groupId)")
            
            // 3. Copy data from old table to new table
            database.execSQL("""
                INSERT INTO token_metadata_new (contractAddress, chainId, decimals, name, symbol, logo, swappable, groupId)
                SELECT contractAddress, chainId, decimals, name, symbol, logo, swappable, groupId
                FROM token_metadata
            """)
            
            // 4. Drop the old table
            database.execSQL("DROP TABLE token_metadata")
            
            // 5. Rename the new table to the original name
            database.execSQL("ALTER TABLE token_metadata_new RENAME TO token_metadata")
        }
    }
    
    /**
     * Migration from version 5 to 6:
     * - Adds unique index to token_exchange table to prevent duplicate entries
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // First, drop the old index if it exists (in case of re-migration)
            database.execSQL("DROP INDEX IF EXISTS index_token_exchange_address_chainId_currency_timestamp")
            
            // Clean up duplicate exchange rates - keep only the latest one per token/currency
            database.execSQL("""
                DELETE FROM token_exchange 
                WHERE id NOT IN (
                    SELECT MAX(id) 
                    FROM token_exchange 
                    GROUP BY address, chainId, currency
                )
            """)
            
            // Create unique index to prevent duplicate exchange rate entries
            // We only need one exchange rate per token per currency
            database.execSQL("""
                CREATE UNIQUE INDEX IF NOT EXISTS index_token_exchange_address_chainId_currency 
                ON token_exchange(address, chainId, currency)
            """)
        }
    }
    
    /**
     * All migrations for the database.
     * Add new migrations here as the schema evolves.
     */
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6
        // Future migrations will be added here: MIGRATION_6_7, etc.
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