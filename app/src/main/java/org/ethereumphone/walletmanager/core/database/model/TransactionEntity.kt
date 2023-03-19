package org.ethereumphone.walletmanager.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction")
data class TransactionEntity(
    @PrimaryKey
    val chainID: String,
    val blockNumber: Int,
    val hash: String,
    val nonce: Int,
    val blockHash: String,
    val from: String,
    val to: String,
    val value: Int,
    val gas: Int,
    val gasPrice: Int,
    val isError: Int,
    @ColumnInfo(name = "txreceipt_status")
    val txReceiptStatus: Int,
    val input: String,
    val contractAddress: String,
    val cumulativeGasUsed: Int,
    val gasUsed: Int,
    val confirmations: Int,
    val methodId: String,
    val functionName: String
)