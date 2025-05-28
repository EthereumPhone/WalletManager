package com.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Room entity for caching ENS name resolutions
 * Stores the mapping between Ethereum addresses and their ENS names
 */
@Entity(tableName = "ens_cache")
data class EnsEntity(
    @PrimaryKey
    val address: String,
    val ensName: String?,
    val timestamp: Instant = Clock.System.now()
) 