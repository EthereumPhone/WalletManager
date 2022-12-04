package org.ethereumphone.walletmanager.models

data class Transaction(
    val type: Boolean, // true is "to", false is "from"
    val value: String,
    val hash: String,
    val toAddr: String,
    val fromAddr: String
)
