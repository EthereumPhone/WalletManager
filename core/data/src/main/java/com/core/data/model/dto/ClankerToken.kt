package com.core.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ClankerToken(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("last_indexed") val lastIndexed: String,
    val admin: String,
    @SerialName("tx_hash") val txHash: String,
    @SerialName("contract_address") val contractAddress: String,
    val name: String,
    val symbol: String,
    val description: String,
    val socialLinks: List<String>,
    val supply: String,
    @SerialName("img_url") val imgUrl: String,
    @SerialName("pool_address") val poolAddress: String,
    val type: String,
    val pair: String,
    @SerialName("chain_id") val chainId: Long,
    val metadata: Metadata,
    @SerialName("deployed_at") val deployedAt: String,
    @SerialName("msg_sender") val msgSender: String,
    @SerialName("factory_address") val factoryAddress: String,
    @SerialName("locker_address") val lockerAddress: String,
    @SerialName("position_id") val positionId: String,
    val warnings: List<String>,
    @SerialName("pool_config") val poolConfig: PoolConfig,
    @SerialName("starting_market_cap") val startingMarketCap: Double,
    val tags: Tags,
    val extensions: Extensions,
    val vault: Vault,
    val related: Related
)

@kotlinx.serialization.Serializable
data class Metadata(
    val auditUrls: List<String>,
    val description: String,
    val socialMediaUrls: List<String>
)

@Serializable
data class PoolConfig(
    val pairedToken: String,
    val tickIfToken0IsNewToken: Long
)

@Serializable
data class Tags(
    val champagne: Boolean,
    val verified: Boolean
)

@Serializable
data class Extensions(
    val fees: Fees
)

@Serializable
data class Fees(
    val type: String,
    val clankerFee: Long,
    val pairedFee: Long,
    @SerialName("hook_address") val hookAddress: String,
    val recipients: List<Recipient>
)

@Serializable
data class Recipient(
    val bps: Int,
    val admin: String,
    val recipient: String
)

@Serializable
data class Vault(
    val amount: String,
    val lockup: Lockup
)

@Serializable
data class Lockup(
    @SerialName("startedAt") val startedAt: Long,
    @SerialName("lockDuration") val lockDuration: Long,
    @SerialName("vestDuration") val vestDuration: Long
)

@Serializable
class Related