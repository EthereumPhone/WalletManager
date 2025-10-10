package com.core.data.model

import com.core.data.model.dto.ClankerToken

interface ClankerDataSource {

    suspend fun getClankerTokens(
        query: String,
        pageSize: Int = 10,
        pageIndex: Int = 1,
        startAfter: String = ""
    ): List<ClankerToken>
}