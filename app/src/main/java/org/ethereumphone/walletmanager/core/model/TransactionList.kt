package org.ethereumphone.walletmanager.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.ethereumphone.walletmanager.core.model.Transaction

@JsonClass(generateAdapter = true)
data class TransactionList(
    @Json(name = "status")
    val status: String,
    @Json(name = "message")
    val message: String,
    @Json(name = "result")
    val result: List<Transaction>
)