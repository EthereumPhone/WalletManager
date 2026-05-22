package com.core.database.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlinx.serialization.Serializable

// Data classes for JSON parsing

// Uniswap token list format
@Serializable
data class UniswapTokenList(
    val name: String,
    val timestamp: String,
    val version: Version,
    val tags: Map<String, String> = emptyMap(),
    val logoURI: String? = null,
    val keywords: List<String> = emptyList(),
    val tokens: List<TokenJson>
)

@Serializable
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int
)

@Serializable
data class TokenJson(
    val chainId: Int,
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String? = null,
    val extensions: Extensions? = null
)

@Serializable
data class Extensions(
    val bridgeInfo: Map<String, BridgeInfo>? = null
)

@Serializable
data class BridgeInfo(
    val tokenAddress: String
)

// Internal data classes for processing
data class BridgeRelationship(
    val sourceChain: Int,
    val sourceAddress: String,
    val targetChain: Int,
    val targetAddress: String,
    val sourceToken: TokenJson
)

data class TokenGroup(
    val id: String,
    val canonicalChain: Int,
    val canonicalAddress: String,
    val symbol: String,
    val name: String,
    val tokens: MutableSet<Pair<Int, String>>,
    val bridges: MutableList<BridgeRelationship>
)


private class DSU(tokens: Set<Pair<Int, String>>) {
    private val parent = tokens.associateWith { it }.toMutableMap()
    private val size = tokens.associateWith { 1 }.toMutableMap()

    fun find(key: Pair<Int, String>): Pair<Int, String> {
        if (!parent.containsKey(key)) {
            parent[key] = key
            size[key] = 1
            return key
        }
        if (parent[key] == key) return key
        parent[key] = find(parent[key]!!)
        return parent[key]!!
    }

    fun union(key1: Pair<Int, String>, key2: Pair<Int, String>) {
        val root1 = find(key1)
        val root2 = find(key2)
        if (root1 != root2) {
            if (size.getOrDefault(root1, 1) < size.getOrDefault(root2, 1)) {
                parent[root1] = root2
                size[root2] = size.getOrDefault(root2, 1) + size.getOrDefault(root1, 1)
            } else {
                parent[root2] = root1
                size[root1] = size.getOrDefault(root1, 1) + size.getOrDefault(root2, 1)
            }
        }
    }
}


object TokenSeedingHelper {
    
    private const val TAG = "TokenSeedingHelper"
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Seeds tokens directly into the database using SQL commands.
     * This approach works from database callbacks where DAOs aren't available.
     */
    suspend fun seedTokensDirectly(context: Context, database: SupportSQLiteDatabase) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting direct token seeding...")
            
            // Check if tokens already exist
            val cursor = database.query("SELECT COUNT(*) FROM token_metadata WHERE groupId IS NOT NULL")
            cursor.use {
                if (it.moveToFirst() && it.getInt(0) > 0) {
                    Log.d(TAG, "Tokens already seeded, skipping")
                    return@withContext
                }
            }
            
            // Load the single Uniswap token list
            val tokenList = loadUniswapTokenList(context)
            if (tokenList == null) {
                Log.e(TAG, "Failed to load Uniswap token list")
                return@withContext
            }
            
            val allTokens = mutableMapOf<Pair<Int, String>, TokenJson>()
            val bridgeRelationships = mutableListOf<BridgeRelationship>()
            
            Log.d(TAG, "Loaded ${tokenList.tokens.size} tokens from Uniswap list")
            
            // Process all tokens from the list
            tokenList.tokens.forEach { token ->
                val key = token.chainId to token.address.lowercase()
                allTokens[key] = token
                
                // Collect bridge relationships
                token.extensions?.bridgeInfo?.forEach { (targetChainStr, target) ->
                    val targetChain = targetChainStr.toIntOrNull() ?: return@forEach
                    bridgeRelationships.add(
                        BridgeRelationship(
                            sourceChain = token.chainId,
                            sourceAddress = token.address,
                            targetChain = targetChain,
                            targetAddress = target.tokenAddress,
                            sourceToken = token
                        )
                    )
                }
            }

            // Add placeholder tokens for bridge targets that don't exist in our token lists
            // This ensures foreign key constraints are satisfied. Tokens are keyed by
            // (chainId, address): the SAME address can legitimately exist on multiple chains
            // (e.g. DAI shares 0xda1000... on Optimism and Arbitrum), so we only skip when the
            // exact (chain, address) pair already exists — never just because the address is
            // reused elsewhere.
            val missingTokens = mutableSetOf<Pair<Int, String>>()

            bridgeRelationships.forEach { bridge ->
                val sourceKey = bridge.sourceChain to bridge.sourceAddress.lowercase()
                val targetKey = bridge.targetChain to bridge.targetAddress.lowercase()

                // Add placeholder for missing source tokens
                if (!allTokens.containsKey(sourceKey)) {
                    missingTokens.add(sourceKey)
                    allTokens[sourceKey] = TokenJson(
                        chainId = bridge.sourceChain,
                        address = bridge.sourceAddress,
                        name = "${bridge.sourceToken.name} (Bridge Source)",
                        symbol = bridge.sourceToken.symbol,
                        decimals = bridge.sourceToken.decimals,
                        logoURI = bridge.sourceToken.logoURI,
                        extensions = null
                    )
                }

                // Add placeholder for missing target tokens
                if (!allTokens.containsKey(targetKey)) {
                    missingTokens.add(targetKey)
                    allTokens[targetKey] = TokenJson(
                        chainId = bridge.targetChain,
                        address = bridge.targetAddress,
                        name = "${bridge.sourceToken.name} (Bridged)",
                        symbol = bridge.sourceToken.symbol,
                        decimals = bridge.sourceToken.decimals,
                        logoURI = bridge.sourceToken.logoURI,
                        extensions = null
                    )
                }
            }
            Log.d(TAG, "Added ${missingTokens.size} placeholder tokens for bridge endpoints")

            // Build token groups
            val tokenGroups = buildTokenGroups(allTokens, bridgeRelationships)
            
            // Insert into database using transactions
            database.beginTransaction()
            try {
                // Step 1: Insert all tokens into token_metadata first (without groupId)
                Log.d(TAG, "Step 1: Inserting ${allTokens.size} tokens...")
                
                // First, check for duplicate contract addresses across chains
                val addressToChains = mutableMapOf<String, MutableList<Int>>()
                allTokens.forEach { (key, token) ->
                    val address = token.address.lowercase()
                    addressToChains.getOrPut(address) { mutableListOf() }.add(token.chainId)
                }
                
                val duplicateAddresses = addressToChains.filter { it.value.size > 1 }
                if (duplicateAddresses.isNotEmpty()) {
                    Log.w(TAG, "Found ${duplicateAddresses.size} contract addresses used on multiple chains:")
                    duplicateAddresses.forEach { (address, chains) ->
                        Log.w(TAG, "  Address $address is used on chains: ${chains.joinToString(", ")}")
                    }
                }
                
                // Insert tokens. The primary key is (contractAddress, chainId), so the same
                // address may legitimately appear on multiple chains. Track inserted
                // (chainId, address) pairs — keying dedup on address alone (the old behaviour)
                // silently dropped a token's row on every chain after the first, hiding the
                // balance for users who held it on the "second" chain.
                val insertedTokens = mutableSetOf<Pair<Int, String>>()

                allTokens.forEach { (key, token) ->
                    val address = token.address.lowercase()
                    val tokenKey = token.chainId to address

                    if (insertedTokens.contains(tokenKey)) return@forEach

                    try {
                        database.execSQL(
                            """
                            INSERT OR IGNORE INTO token_metadata
                            (contractAddress, decimals, name, symbol, logo, chainId, swappable, groupId)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                            arrayOf(
                                address,
                                token.decimals,
                                token.name,
                                token.symbol,
                                token.logoURI,
                                token.chainId,
                                1, // swappable = true
                                null // groupId will be updated later
                            )
                        )
                        insertedTokens.add(tokenKey)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to insert token ${token.symbol} (${token.address}) on chain ${token.chainId}", e)
                        throw e
                    }
                }
                
                // Step 2: Insert token groups
                Log.d(TAG, "Step 2: Inserting ${tokenGroups.size} token groups...")
                tokenGroups.values.forEach { group ->
                    try {
                        database.execSQL(
                            """
                            INSERT OR IGNORE INTO token_group (groupId, canonicalChainId, canonicalAddress, symbol, name)
                            VALUES (?, ?, ?, ?, ?)
                            """,
                            arrayOf(group.id, group.canonicalChain, group.canonicalAddress.lowercase(), group.symbol, group.name)
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to insert token group ${group.id}", e)
                        throw e
                    }
                }
                
                // Step 3: Insert bridges (now that all tokens exist)
                Log.d(TAG, "Step 3: Inserting bridges...")
                var bridgesInserted = 0
                var bridgesSkipped = 0
                tokenGroups.values.forEach { group ->
                    group.bridges.forEach { bridge ->
                        val sourceAddress = bridge.sourceAddress.lowercase()
                        val targetAddress = bridge.targetAddress.lowercase()

                        // Skip unless BOTH endpoints were actually seeded on the bridge's chains.
                        val shouldSkip = (bridge.sourceChain to sourceAddress) !in insertedTokens ||
                                        (bridge.targetChain to targetAddress) !in insertedTokens

                        if (shouldSkip) {
                            bridgesSkipped++
                            Log.w(TAG, "Skipping bridge ${bridge.sourceChain}:$sourceAddress -> ${bridge.targetChain}:$targetAddress (endpoint not seeded)")
                            return@forEach
                        }
                        
                        try {
                            // Log specific bridge details for debugging
                            if (sourceAddress == "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2" && 
                                targetAddress == "0x4200000000000000000000000000000000000006") {
                                Log.d(TAG, "Attempting to insert WETH bridge: ${bridge.sourceChain}:$sourceAddress -> ${bridge.targetChain}:$targetAddress")
                            }
                            
                            database.execSQL(
                                """
                                INSERT OR IGNORE INTO token_bridge (sourceChainId, sourceAddress, targetChainId, targetAddress, groupId)
                                VALUES (?, ?, ?, ?, ?)
                                """,
                                arrayOf(bridge.sourceChain, sourceAddress, bridge.targetChain, targetAddress, group.id)
                            )
                            bridgesInserted++
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to insert bridge: ${bridge.sourceChain}:${bridge.sourceAddress} -> ${bridge.targetChain}:${bridge.targetAddress}", e)
                            
                            // Detailed debugging for foreign key issues
                            Log.e(TAG, "Debugging foreign key constraint failure:")
                            
                            // Check source token
                            val sourceCursor = database.query(
                                "SELECT contractAddress, chainId FROM token_metadata WHERE contractAddress = ?", 
                                arrayOf(sourceAddress)
                            )
                            sourceCursor.use {
                                if (it.moveToFirst()) {
                                    val actualChainId = it.getInt(1)
                                    Log.e(TAG, "  Source: Found token $sourceAddress with chainId=$actualChainId (bridge expects chainId=${bridge.sourceChain})")
                                } else {
                                    Log.e(TAG, "  Source: Token $sourceAddress NOT FOUND in database")
                                }
                            }
                            
                            // Check target token
                            val targetCursor = database.query(
                                "SELECT contractAddress, chainId FROM token_metadata WHERE contractAddress = ?", 
                                arrayOf(targetAddress)
                            )
                            targetCursor.use {
                                if (it.moveToFirst()) {
                                    val actualChainId = it.getInt(1)
                                    Log.e(TAG, "  Target: Found token $targetAddress with chainId=$actualChainId (bridge expects chainId=${bridge.targetChain})")
                                } else {
                                    Log.e(TAG, "  Target: Token $targetAddress NOT FOUND in database")
                                }
                            }
                            
                            // Don't rethrow - just skip this bridge
                            Log.e(TAG, "Skipping this bridge due to foreign key constraint")
                            bridgesSkipped++
                        }
                    }
                }
                
                Log.d(TAG, "Bridge insertion complete: $bridgesInserted inserted, $bridgesSkipped skipped")
                
                // Step 4: Update token metadata with group IDs
                Log.d(TAG, "Step 4: Updating token group IDs...")
                tokenGroups.values.forEach { group ->
                    group.tokens.forEach { key ->
                        val token = allTokens[key]
                        if (token != null) {
                            val address = token.address.lowercase()

                            // Skip if this (chain, address) wasn't inserted
                            if ((token.chainId to address) !in insertedTokens) {
                                return@forEach
                            }

                            try {
                                // PK is (contractAddress, chainId) — scope the update to this
                                // exact row so same-address tokens on other chains aren't touched.
                                database.execSQL(
                                    """
                                    UPDATE token_metadata
                                    SET groupId = ?
                                    WHERE contractAddress = ? AND chainId = ?
                                    """,
                                    arrayOf(group.id, address, token.chainId)
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to update groupId for token $address on chain ${token.chainId}", e)
                            }
                        }
                    }
                }
                
                database.setTransactionSuccessful()
                Log.d(TAG, "Successfully seeded ${insertedTokens.size} tokens (from ${allTokens.size} total) and ${tokenGroups.size} token groups")
            } finally {
                database.endTransaction()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding tokens", e)
            throw e
        }
    }
    
    private fun loadUniswapTokenList(context: Context): UniswapTokenList? {
        return try {
            val resourceId = context.resources.getIdentifier(
                "tokens_uniswap_org",
                "raw",
                context.packageName
            )
            if (resourceId == 0) {
                Log.w(TAG, "Uniswap token list not found")
                return null
            }
            context.resources.openRawResource(resourceId).use { stream ->
                json.decodeFromString<UniswapTokenList>(stream.readBytes().decodeToString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading Uniswap token list", e)
            null
        }
    }
    
    private fun buildTokenGroups(
        allTokens: Map<Pair<Int, String>, TokenJson>,
        bridges: List<BridgeRelationship>
    ): Map<String, TokenGroup> {
        val tokenKeys = allTokens.keys
        val dsu = DSU(tokenKeys)

        // 1. Group by symbol (case-insensitive)
        val tokensBySymbol = allTokens.values.groupBy { it.symbol.lowercase() }
        tokensBySymbol.values.forEach { tokensWithSameSymbol ->
            if (tokensWithSameSymbol.size > 1) {
                for (i in 0 until tokensWithSameSymbol.size - 1) {
                    val key1 = tokensWithSameSymbol[i].chainId to tokensWithSameSymbol[i].address.lowercase()
                    val key2 = tokensWithSameSymbol[i + 1].chainId to tokensWithSameSymbol[i + 1].address.lowercase()
                    dsu.union(key1, key2)
                }
            }
        }

        // 2. Group by bridge info
        bridges.forEach { bridge ->
            val sourceKey = bridge.sourceChain to bridge.sourceAddress.lowercase()
            val targetKey = bridge.targetChain to bridge.targetAddress.lowercase()
            dsu.union(sourceKey, targetKey)
        }

        // 3. Create groups from DSU sets
        val disjointSets = tokenKeys.groupBy { dsu.find(it) }
        val tokenGroups = mutableMapOf<String, TokenGroup>()

        disjointSets.forEach { (rootKey, tokenSetKeys) ->
            // Try to find a canonical token from chain 1 (mainnet), otherwise use the root of the set.
            val canonicalKey = tokenSetKeys.find { it.first == 1 } ?: rootKey
            val canonicalToken = allTokens[canonicalKey]!!

            val groupId = generateGroupId(canonicalToken)

            val groupBridges = bridges.filter { bridge ->
                val sourceKey = bridge.sourceChain to bridge.sourceAddress.lowercase()
                tokenSetKeys.contains(sourceKey)
            }.toMutableList()

            tokenGroups[groupId] = TokenGroup(
                id = groupId,
                canonicalChain = canonicalToken.chainId,
                canonicalAddress = canonicalToken.address.lowercase(),
                symbol = canonicalToken.symbol,
                name = canonicalToken.name,
                tokens = tokenSetKeys.toMutableSet(),
                bridges = groupBridges
            )
        }

        return tokenGroups
    }
    
    private fun generateGroupId(token: TokenJson): String {
        return "${token.chainId}_${token.address.lowercase()}"

    }
}
