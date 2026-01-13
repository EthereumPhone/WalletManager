package com.core.data.repository

interface SearchRepository {

    suspend fun queryTokens(q: String)
}