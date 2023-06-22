package com.core.data.model.requestBody

data class TokenMetadataRequestBody(
    val id: Int = 1,
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenMetadata",
    val params: List<String> // list of erc20 token contracts
)