package com.core.model

data class UserData(
    val walletAddress: String,
    val walletNetwork: String,
    val onboardingCompleted: Boolean,
    val preferredCurrency: String
)
