package com.core.data.model.dto

import java.math.BigInteger

data class EthTransferDto(
    val toAddress: String,
    val amount: Float,
    val gasPrice: BigInteger?,

    )