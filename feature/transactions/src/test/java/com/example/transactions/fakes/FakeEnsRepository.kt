package com.example.transactions.fakes

import com.core.data.repository.EnsRepository

class FakeEnsRepository : EnsRepository {

    private val ensMap = mutableMapOf<String, String?>()

    override suspend fun getEnsName(address: String): String? =
        ensMap[address.lowercase()]

    override suspend fun getEnsNames(addresses: List<String>): Map<String, String?> =
        addresses.associateWith { ensMap[it.lowercase()] }

    override suspend fun saveEnsName(address: String, ensName: String?) {
        ensMap[address.lowercase()] = ensName
    }

    override suspend fun saveEnsNames(ensMappings: List<Pair<String, String?>>) {
        ensMappings.forEach { (address, name) ->
            ensMap[address.lowercase()] = name
        }
    }
}




