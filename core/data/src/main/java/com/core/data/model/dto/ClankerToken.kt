package com.core.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ClankerToken(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("last_indexed") val lastIndexed: String,
    val admin: String? = null,
    @SerialName("tx_hash") val txHash: String? = null,
    @SerialName("contract_address") val contractAddress: String,
    val name: String,
    val symbol: String,
    val description: String? = null,
    val socialLinks: List<String>? = null,
    val supply: String? = null,
    @SerialName("img_url") val imgUrl: String? = null,
    @SerialName("pool_address") val poolAddress: String? = null,
    val type: String? = null,
    val pair: String? = null,
    @SerialName("chain_id") val chainId: Long? = null,
    val metadata: Metadata? = null,
    @SerialName("deployed_at") val deployedAt: String? = null,
    @SerialName("msg_sender") val msgSender: String? = null,
    @SerialName("factory_address") val factoryAddress: String? = null,
    @SerialName("locker_address") val lockerAddress: String? = null,
    @SerialName("position_id") val positionId: String? = null,
    val warnings: List<String>? = null,
    @SerialName("pool_config") val poolConfig: PoolConfig? = null,
    @SerialName("starting_market_cap") val startingMarketCap: Double? = null,
    val tags: Tags? = null,
    val extensions: Extensions? = null,
    val vault: Vault? = null,
    val related: Related? = null
)

@kotlinx.serialization.Serializable
data class Metadata(
    val auditUrls: List<String>? = null,
    val description: String? = null,
    val socialMediaUrls: List<String>? = null
)

@Serializable
data class PoolConfig(
    val pairedToken: String? = null,
    val tickIfToken0IsNewToken: Long? = null
)

@Serializable
data class Tags(
    val champagne: Boolean? = null,
    val verified: Boolean? = null
)

@Serializable
data class Extensions(
    val fees: Fees? = null
)

@Serializable
data class Fees(
    val type: String? = null,
    val clankerFee: Long? = null,
    val pairedFee: Long? = null,
    @SerialName("hook_address") val hookAddress: String? = null,
    val recipients: List<Recipient>? = null
)

@Serializable
data class Recipient(
    val bps: Int? = null,
    val admin: String? = null,
    val recipient: String? = null
)

@Serializable
data class Vault(
    val amount: String? = null,
    val lockup: Lockup? = null
)

@Serializable
data class Lockup(
    @SerialName("startedAt") val startedAt: Long? = null,
    @SerialName("lockDuration") val lockDuration: Long? = null,
    @SerialName("vestDuration") val vestDuration: Long? = null
)

@Serializable
class Related