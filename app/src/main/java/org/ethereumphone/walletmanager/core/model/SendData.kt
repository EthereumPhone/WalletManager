package org.ethereumphone.walletmanager.core.model

import java.math.BigInteger

data class SendData(
    val amount: Float,
    val gasPrice: BigInteger?,
    val address: String
)