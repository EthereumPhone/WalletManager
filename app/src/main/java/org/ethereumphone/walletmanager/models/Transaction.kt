package org.ethereumphone.walletmanager.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Transaction(
    @Json(name = "blockNumber")
    val blockNumber: String,
    @Json(name = "timeStamp")
    val timeStamp: String,
    @Json(name = "hash")
    val hash: String,
    @Json(name = "nonce")
    val nonce: String,
    @Json(name = "blockHash")
    val blockHash: String,
    @Json(name = "transactionIndex")
    val transactionIndex: String,
    @Json(name = "from")
    val from: String,
    @Json(name = "to")
    val to: String,
    @Json(name = "value")
    var value: String,
    @Json(name = "gas")
    val gas: String,
    @Json(name = "gasPrice")
    val gasPrice: String,
    @Json(name = "isError")
    val isError: String,
    @Json(name = "txreceipt_status")
    val txReceiptStatus: String,
    @Json(name = "input")
    val input: String,
    @Json(name = "contractAddress")
    val contractAddress: String,
    @Json(name = "cumulativeGasUsed")
    val cumulativeGasUsed: String,
    @Json(name = "gasUsed")
    val gasUsed: String,
    @Json(name = "confirmations")
    val confirmations: String,
    @Json(name = "methodId")
    val methodId: String,
    @Json(name = "functionName")
    val functionName: String,
)
