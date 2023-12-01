package com.core.database.model

import androidx.room.Entity
import com.core.model.Exchange

@Entity(primaryKeys = ["base", "currency"])
data class ExchangeEntity(
    val amount: String,
    val base: String,
    val currency: String,
)

fun ExchangeEntity.asExternalModel() = Exchange(
    amount,
    base,
    currency
)