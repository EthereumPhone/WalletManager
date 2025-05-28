package com.core.data.repository

import com.core.database.dao.EnsDao
import com.core.database.model.EnsEntity
import javax.inject.Inject

class EnsRepositoryImpl @Inject constructor(
    private val ensDao: EnsDao
) : EnsRepository {
    
    override suspend fun getEnsName(address: String): String? {
        return ensDao.getEnsForAddress(address.lowercase())?.ensName
    }
    
    override suspend fun getEnsNames(addresses: List<String>): Map<String, String?> {
        val normalizedAddresses = addresses.map { it.lowercase() }
        val entities = ensDao.getEnsForAddresses(normalizedAddresses)
        return entities.associate { it.address to it.ensName }
    }
    
    override suspend fun saveEnsName(address: String, ensName: String?) {
        ensDao.insertEns(
            EnsEntity(
                address = address.lowercase(),
                ensName = ensName
            )
        )
    }
    
    override suspend fun saveEnsNames(ensMappings: List<Pair<String, String?>>) {
        val entities = ensMappings.map { (address, ensName) ->
            EnsEntity(
                address = address.lowercase(),
                ensName = ensName
            )
        }
        ensDao.insertMultipleEns(entities)
    }
} 