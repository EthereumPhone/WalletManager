package com.core.model

data class TransferItem(
    val chainId: Int,
    val address: String,
    val asset: String,
    val value: String,
    val timeStamp: String,
    val userSent: Boolean
)