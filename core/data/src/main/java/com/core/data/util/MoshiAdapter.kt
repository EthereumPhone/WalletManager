package com.core.data.util

import com.core.data.model.dto.TokenBalanceDto
import com.core.data.model.dto.TokenBalanceJsonResponse
import com.core.data.model.dto.TokenMetadataDto
import com.core.data.model.dto.TokenMetadataJsonResponse
import com.core.data.model.dto.TransferDto
import com.core.data.model.dto.TransferJsonResponse
import com.squareup.moshi.FromJson

class MoshiAdapters {

    @FromJson
    fun toTokenBalanceDto(
        tokenBalanceJsonResponse: TokenBalanceJsonResponse
    ): List<TokenBalanceDto> {
        return tokenBalanceJsonResponse.result.tokenBalances
    }

    @FromJson
    fun toTransferDto(
        transferJsonResponse: TransferJsonResponse
    ): List<TransferDto> {
        return transferJsonResponse.result.transfers
    }

    @FromJson
    fun toTokenMetadataDto(
        tokenMetadataJsonResponse: TokenMetadataJsonResponse
    ): TokenMetadataDto {
        return tokenMetadataJsonResponse.result
    }
}