package com.core.data.repository

interface EnsRepository {
    suspend fun getEnsName(address: String): String?
    suspend fun getEnsNames(addresses: List<String>): Map<String, String?>
    suspend fun saveEnsName(address: String, ensName: String?)
    suspend fun saveEnsNames(ensMappings: List<Pair<String, String?>>)
} 