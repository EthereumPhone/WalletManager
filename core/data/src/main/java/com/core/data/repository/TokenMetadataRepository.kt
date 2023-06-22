package com.core.data.repository

import com.core.Resource.Resource
import com.core.model.TokenBalance
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow

interface TokenMetadataRepository {
    fun getTokenMetadataByContractAddress(
        fetchRemote: Boolean,
        contractAddress: List<TokenBalance>
    ): Flow<Resource<List<TokenMetadata>>>

}