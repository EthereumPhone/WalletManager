package com.core.model


data class UserData(
    val walletAddress: String,
    val walletNetwork: String,
    val isFirstBoot: Boolean = true,
    val preferredCurrency: String,
)
