package com.core.database.util

import android.content.Context
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.*
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlinx.serialization.Serializable



class UniswapTokenSeederHelper(
    private val context: Context,
    private val tokenGroupDao: TokenGroupDao,
    private val tokenMetadataDao: TokenMetadataDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedTokens() {
        // Load the single Uniswap token list
        val tokenList = loadUniswapTokenList()
        if (tokenList == null) {
            println("Failed to load Uniswap token list")
            return
        }

        val allTokens = mutableMapOf<Pair<Int, String>, TokenJson>()
        val bridgeRelationships = mutableListOf<BridgeRelationship>()

        println("Loaded ${tokenList.tokens.size} tokens from Uniswap list")

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

        // Build token groups from bridge relationships
        val tokenGroups = buildTokenGroups(allTokens, bridgeRelationships)

        // Insert data into database
        insertTokenGroups(tokenGroups, allTokens)
    }

    private fun buildTokenGroups(
        allTokens: Map<Pair<Int, String>, TokenJson>,
        bridges: List<BridgeRelationship>
    ): Map<String, TokenGroup> {
        val groups = mutableMapOf<String, TokenGroup>()
        val tokenToGroup = mutableMapOf<Pair<Int, String>, String>()

        // Process bridge relationships to create groups
        bridges.forEach { bridge ->
            val sourceKey = bridge.sourceChain to bridge.sourceAddress.lowercase()
            val targetKey = bridge.targetChain to bridge.targetAddress.lowercase()

            val existingGroupId = tokenToGroup[sourceKey] ?: tokenToGroup[targetKey]

            val groupId = if (existingGroupId != null) {
                existingGroupId
            } else {
                // Create new group ID based on canonical token (mainnet if available)
                val canonicalToken = if (bridge.sourceChain == 1) {
                    bridge.sourceToken
                } else {
                    allTokens[targetKey] ?: bridge.sourceToken
                }
                generateGroupId(canonicalToken)
            }

            // Add tokens to group
            tokenToGroup[sourceKey] = groupId
            tokenToGroup[targetKey] = groupId

            // Update or create group
            val group = groups.getOrPut(groupId) {
                TokenGroup(
                    id = groupId,
                    canonicalChain = if (bridge.sourceChain == 1) bridge.sourceChain else bridge.targetChain,
                    canonicalAddress = if (bridge.sourceChain == 1) bridge.sourceAddress else bridge.targetAddress,
                    symbol = bridge.sourceToken.symbol,
                    name = bridge.sourceToken.name,
                    tokens = mutableSetOf(),
                    bridges = mutableListOf()
                )
            }

            group.tokens.add(sourceKey)
            group.tokens.add(targetKey)
            group.bridges.add(bridge)
        }

        // Add ungrouped tokens as singleton groups
        allTokens.forEach { (key, token) ->
            if (!tokenToGroup.containsKey(key)) {
                val groupId = generateGroupId(token)
                groups[groupId] = TokenGroup(
                    id = groupId,
                    canonicalChain = key.first,
                    canonicalAddress = key.second,
                    symbol = token.symbol,
                    name = token.name,
                    tokens = mutableSetOf(key),
                    bridges = mutableListOf()
                )
                tokenToGroup[key] = groupId
            }
        }

        return groups
    }

    private fun generateGroupId(token: TokenJson): String {
        return "${token.chainId}_${token.address.lowercase()}"
    }

    private suspend fun insertTokenGroups(
        groups: Map<String, TokenGroup>,
        allTokens: Map<Pair<Int, String>, TokenJson>
    ) {
        groups.values.forEach { group ->
            // Create TokenGroupEntity
            val groupEntity = TokenGroupEntity(
                groupId = group.id,
                canonicalChainId = group.canonicalChain,
                canonicalAddress = group.canonicalAddress,
                symbol = group.symbol,
                name = group.name,
            )

            // Create TokenBridgeEntities
            val bridgeEntities = group.bridges.map { bridge ->
                TokenBridgeEntity(
                    sourceChainId = bridge.sourceChain,
                    sourceAddress = bridge.sourceAddress,
                    targetChainId = bridge.targetChain,
                    targetAddress = bridge.targetAddress,
                    groupId = group.id,
                )
            }

            // Update token metadata with group IDs
            val tokenUpdates = group.tokens.mapNotNull { key ->
                allTokens[key]?.let { token ->
                    TokenMetadataEntity(
                        contractAddress = token.address,
                        decimals = token.decimals,
                        name = token.name,
                        symbol = token.symbol,
                        logo = token.logoURI,
                        chainId = token.chainId,
                        swappable = true,
                        groupId = group.id
                    )
                }
            }

            tokenMetadataDao.upsertTokensMetadata(tokenUpdates)

            // Insert everything in a transaction
            tokenGroupDao.createTokenGroupWithBridges(
                group = groupEntity,
                bridges = bridgeEntities,
                tokenUpdates = tokenUpdates
            )
        }
    }

    private fun loadUniswapTokenList(): UniswapTokenList? {
        return try {
            val resourceId = context.resources.getIdentifier(
                "tokens_uniswap_org",
                "raw",
                context.packageName
            )
            context.resources.openRawResource(resourceId).use { stream ->
                json.decodeFromString<UniswapTokenList>(stream.readBytes().decodeToString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

