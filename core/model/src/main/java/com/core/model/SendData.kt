package com.core.model

import java.math.BigInteger

data class SendData(
    val amount: Float,
    val address: String,
    val chainId: Int
)