package com.core.data.repository

import com.core.Resource.Resource
import com.core.data.model.dto.TransferDto
import com.core.model.TokenBalance
import com.core.model.TokenMetadata
import com.core.model.Transfer
import kotlinx.coroutines.flow.Flow

interface TokenBalanceRepository {

    fun getAllTokenBalances(
        fetchRemote: Boolean,
        toAddress: String
    ): Flow<Resource<List<TokenBalance>>>
    fun getTokenBalanceByContractAddress(
        fetchRemote: Boolean,
        toAddress: String,
        contractAddress: List<TokenBalance>
        ): Flow<Resource<List<TokenBalance>>>

}