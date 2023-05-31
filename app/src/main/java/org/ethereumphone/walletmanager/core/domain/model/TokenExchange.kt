package org.ethereumphone.walletmanager.core.domain.model

import com.google.gson.annotations.JsonAdapter
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

data class TokenExchange(
    val symbol: String,
    val price: Double
)

