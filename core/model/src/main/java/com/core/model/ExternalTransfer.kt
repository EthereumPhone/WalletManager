package com.core.model

data class TransferItem(
    val chainId: Int,
    val from: String,
    val to: String,
    val asset: String,
    val value: String,
    val timeStamp: String,
    val userSent: Boolean
)