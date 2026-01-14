package com.core.data.repository

import com.core.model.NftTokenType
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface SendRepository {

    val currentTransactionHash: Flow<String>
    val currentTransactionChainId: Flow<Int>
    suspend fun transferEth(
        chainId: Int,
        toAddress: String,
        value: String,
        data: String?,
        gasPrice: String? = null,
        gasAmount: String = ""
    )

    suspend fun transferErc20(
        chainId: Int,
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    )

    suspend fun maxAllowedSend(
        amount: BigDecimal,
        chainId: Int
    ): String

    fun restoreState()
    suspend fun getMaxErc20AmountString(
        contractAddress: String,
        chainId: Int,
        decimals: Int
    ): String

    /**
     * Transfer an NFT (ERC721, ERC1155, or ERC404) to another address
     * @param chainId The chain ID where the NFT exists
     * @param contractAddress The NFT contract address
     * @param tokenId The token ID of the NFT
     * @param toAddress The recipient address
     * @param tokenType The type of NFT (ERC721, ERC1155, or ERC404)
     * @param amount The amount to transfer (only used for ERC1155, default is 1)
     */
    suspend fun transferNft(
        chainId: Int,
        contractAddress: String,
        tokenId: String,
        toAddress: String,
        tokenType: NftTokenType = NftTokenType.ERC721,
        amount: Int = 1
    )
}