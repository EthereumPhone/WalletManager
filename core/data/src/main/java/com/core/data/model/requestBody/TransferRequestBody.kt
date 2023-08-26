package com.core.data.model.requestBody

data class NetworkTransferRequestBody(
    val id: Int = 1,
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getAssetTransfers",
    val params: List<NetworkTransferRequestParams>
) {
    data class NetworkTransferRequestParams (
        val fromBlock: String = "0x0",
        val toBlock: String = "latest",
        val toAddress: String ,
        val fromAddress: String,
        val category: List<String> = listOf("external", "internal", "erc20", "erc721", "erc1155"),
        val withMetadata: Boolean = true,
        val excludeZeroValue: Boolean = true,
        val maxCount: String = "0x3e8"
    )
}