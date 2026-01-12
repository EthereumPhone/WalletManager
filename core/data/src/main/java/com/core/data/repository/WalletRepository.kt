package com.core.data.repository

/**
 * Minimal wallet abstraction used by PayMaster to fetch the current wallet address.
 */
interface WalletRepository {
    fun getAddress(): String
}



