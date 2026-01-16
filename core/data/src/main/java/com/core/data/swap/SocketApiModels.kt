package com.core.data.swap

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Socket API Models for Cross-Chain Swaps
 * Documentation: https://docs.socket.tech/socket-api/v2
 * 
 * Socket provides cross-chain bridging and swapping through their aggregator.
 */

// ============ Request Models ============

/**
 * Request body for getting a cross-chain quote
 */
data class SocketQuoteRequest(
    val fromChainId: Int,
    val toChainId: Int,
    val fromTokenAddress: String,
    val toTokenAddress: String,
    val fromAmount: String, // Amount in smallest unit (wei)
    val userAddress: String,
    val uniqueRoutesPerBridge: Boolean = true,
    val sort: String = "output", // "output", "gas", "time"
    val singleTxOnly: Boolean = true
)

// ============ Response Models ============

/**
 * Main response from Socket quote endpoint
 */
@JsonClass(generateAdapter = true)
data class SocketQuoteResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "result") val result: SocketQuoteResult?
)

@JsonClass(generateAdapter = true)
data class SocketQuoteResult(
    @Json(name = "routes") val routes: List<SocketRoute> = emptyList(),
    @Json(name = "fromChainId") val fromChainId: Int,
    @Json(name = "toChainId") val toChainId: Int,
    @Json(name = "fromAsset") val fromAsset: SocketAsset?,
    @Json(name = "toAsset") val toAsset: SocketAsset?
)

@JsonClass(generateAdapter = true)
data class SocketRoute(
    @Json(name = "routeId") val routeId: String,
    @Json(name = "isOnlySwapRoute") val isOnlySwapRoute: Boolean = false,
    @Json(name = "fromAmount") val fromAmount: String,
    @Json(name = "toAmount") val toAmount: String,
    @Json(name = "usedBridgeNames") val usedBridgeNames: List<String> = emptyList(),
    @Json(name = "totalUserTx") val totalUserTx: Int = 1,
    @Json(name = "totalGasFeesInUsd") val totalGasFeesInUsd: Double = 0.0,
    @Json(name = "recipient") val recipient: String? = null,
    @Json(name = "sender") val sender: String? = null,
    @Json(name = "userTxs") val userTxs: List<SocketUserTx> = emptyList(),
    @Json(name = "serviceTime") val serviceTime: Int = 0, // Time in seconds
    @Json(name = "maxServiceTime") val maxServiceTime: Int = 0,
    @Json(name = "integratorFee") val integratorFee: SocketIntegratorFee? = null
)

@JsonClass(generateAdapter = true)
data class SocketUserTx(
    @Json(name = "userTxType") val userTxType: String, // "fund-movr", "dex-swap", etc.
    @Json(name = "txType") val txType: String? = null,
    @Json(name = "chainId") val chainId: Int,
    @Json(name = "toChainId") val toChainId: Int? = null,
    @Json(name = "protocol") val protocol: SocketProtocol? = null,
    @Json(name = "toAsset") val toAsset: SocketAsset? = null,
    @Json(name = "fromAsset") val fromAsset: SocketAsset? = null,
    @Json(name = "fromAmount") val fromAmount: String? = null,
    @Json(name = "toAmount") val toAmount: String? = null,
    @Json(name = "stepCount") val stepCount: Int = 1,
    @Json(name = "routePath") val routePath: String? = null,
    @Json(name = "sender") val sender: String? = null,
    @Json(name = "approvalData") val approvalData: SocketApprovalData? = null,
    @Json(name = "steps") val steps: List<SocketStep> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SocketStep(
    @Json(name = "type") val type: String, // "middleware", "bridge"
    @Json(name = "protocol") val protocol: SocketProtocol? = null,
    @Json(name = "fromChainId") val fromChainId: Int? = null,
    @Json(name = "toChainId") val toChainId: Int? = null,
    @Json(name = "fromAsset") val fromAsset: SocketAsset? = null,
    @Json(name = "toAsset") val toAsset: SocketAsset? = null,
    @Json(name = "fromAmount") val fromAmount: String? = null,
    @Json(name = "toAmount") val toAmount: String? = null,
    @Json(name = "bridgeSlippage") val bridgeSlippage: Double? = null,
    @Json(name = "minAmountOut") val minAmountOut: String? = null
)

@JsonClass(generateAdapter = true)
data class SocketProtocol(
    @Json(name = "name") val name: String,
    @Json(name = "displayName") val displayName: String? = null,
    @Json(name = "icon") val icon: String? = null
)

@JsonClass(generateAdapter = true)
data class SocketAsset(
    @Json(name = "chainId") val chainId: Int,
    @Json(name = "address") val address: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "decimals") val decimals: Int,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "logoURI") val logoURI: String? = null,
    @Json(name = "chainAgnosticId") val chainAgnosticId: String? = null
)

@JsonClass(generateAdapter = true)
data class SocketApprovalData(
    @Json(name = "minimumApprovalAmount") val minimumApprovalAmount: String,
    @Json(name = "approvalTokenAddress") val approvalTokenAddress: String,
    @Json(name = "allowanceTarget") val allowanceTarget: String,
    @Json(name = "owner") val owner: String? = null
)

@JsonClass(generateAdapter = true)
data class SocketIntegratorFee(
    @Json(name = "amount") val amount: String? = null,
    @Json(name = "asset") val asset: SocketAsset? = null
)

// ============ Build Transaction Response ============

/**
 * Response from Socket build-tx endpoint
 */
@JsonClass(generateAdapter = true)
data class SocketBuildTxResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "result") val result: SocketBuildTxResult?
)

@JsonClass(generateAdapter = true)
data class SocketBuildTxResult(
    @Json(name = "userTxType") val userTxType: String,
    @Json(name = "txType") val txType: String? = null,
    @Json(name = "txData") val txData: String,
    @Json(name = "txTarget") val txTarget: String,
    @Json(name = "chainId") val chainId: Int,
    @Json(name = "value") val value: String,
    @Json(name = "userTxIndex") val userTxIndex: Int = 0,
    @Json(name = "totalUserTx") val totalUserTx: Int = 1,
    @Json(name = "approvalData") val approvalData: SocketApprovalData? = null
)

// ============ Token List Response ============

/**
 * Response from Socket token-list endpoint
 */
@JsonClass(generateAdapter = true)
data class SocketTokenListResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "result") val result: List<SocketAsset> = emptyList()
)

// ============ Supported Chains Response ============

@JsonClass(generateAdapter = true)
data class SocketChainsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "result") val result: List<SocketChain> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SocketChain(
    @Json(name = "chainId") val chainId: Int,
    @Json(name = "name") val name: String,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "isL1") val isL1: Boolean = false,
    @Json(name = "sendingEnabled") val sendingEnabled: Boolean = true,
    @Json(name = "receivingEnabled") val receivingEnabled: Boolean = true,
    @Json(name = "currency") val currency: SocketCurrency? = null,
    @Json(name = "rpcs") val rpcs: List<String> = emptyList(),
    @Json(name = "explorers") val explorers: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SocketCurrency(
    @Json(name = "address") val address: String,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "decimals") val decimals: Int
)

// ============ Route Status Response ============

@JsonClass(generateAdapter = true)
data class SocketRouteStatusResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "result") val result: SocketRouteStatus?
)

@JsonClass(generateAdapter = true)
data class SocketRouteStatus(
    @Json(name = "sourceTxStatus") val sourceTxStatus: String, // "PENDING", "COMPLETED"
    @Json(name = "destinationTxStatus") val destinationTxStatus: String,
    @Json(name = "sourceTx") val sourceTx: String? = null,
    @Json(name = "destinationTx") val destinationTx: String? = null,
    @Json(name = "fromChainId") val fromChainId: Int? = null,
    @Json(name = "toChainId") val toChainId: Int? = null
)


