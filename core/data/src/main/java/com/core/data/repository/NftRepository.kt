package com.core.data.repository

import android.util.Log
import com.core.data.BuildConfig
import com.core.data.model.dto.toDomainModel
import com.core.data.remote.NftApi
import com.core.data.service.SoulboundChecker
import com.core.data.util.chainIdToName
import com.core.database.dao.NftDao
import com.core.database.model.NftEntity
import com.core.database.model.asDomainModel
import com.core.database.model.asEntity
import com.core.model.NFT
import com.core.model.NetworkChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for NFT operations
 */
interface NftRepository {
    /**
     * Get all NFTs as a Flow
     */
    fun getNfts(): Flow<List<NFT>>
    
    /**
     * Get NFTs for a specific chain
     */
    fun getNftsByChain(chainId: Int): Flow<List<NFT>>
    
    /**
     * Refresh NFTs from the Alchemy API for all supported chains
     */
    suspend fun refreshNfts(ownerAddress: String)
    
    /**
     * Refresh NFTs for a specific chain
     */
    suspend fun refreshNftsByChain(ownerAddress: String, chainId: Int)
    
    /**
     * Get a specific NFT
     */
    suspend fun getNft(contractAddress: String, tokenId: String, chainId: Int): NFT?
    
    /**
     * Check if any NFTs exist
     */
    fun observeNftsExist(): Flow<Boolean>
    
    /**
     * Remove a specific NFT from the database (after transfer)
     */
    suspend fun removeNft(contractAddress: String, tokenId: String, chainId: Int)
    
    /**
     * Update NFT balance (for ERC1155 tokens after partial transfer)
     */
    suspend fun updateNftBalance(contractAddress: String, tokenId: String, chainId: Int, newBalance: Int)
    
    /**
     * Check if an NFT is soulbound (non-transferable) using EIP-5192.
     * Returns true if the NFT is soulbound and cannot be transferred.
     */
    suspend fun isNftSoulbound(contractAddress: String, tokenId: String, chainId: Int): Boolean
}

/**
 * Default implementation of NftRepository using Alchemy API
 */
@Singleton
class AlchemyNftRepository @Inject constructor(
    private val nftApi: NftApi,
    private val nftDao: NftDao,
    private val soulboundChecker: SoulboundChecker
) : NftRepository {
    
    companion object {
        private const val TAG = "NftRepository"
        
        // Chains that support NFT API
        private val SUPPORTED_CHAINS = listOf(
            NetworkChain.MAINNET,
            NetworkChain.POLYGON,
            NetworkChain.OPTIMISM,
            NetworkChain.ARBITRUM,
            NetworkChain.BASE
        )
    }
    
    override fun getNfts(): Flow<List<NFT>> =
        nftDao.getAllNfts().map { entities ->
            entities.map(NftEntity::asDomainModel)
        }
    
    override fun getNftsByChain(chainId: Int): Flow<List<NFT>> =
        nftDao.getNftsByChain(chainId).map { entities ->
            entities.map(NftEntity::asDomainModel)
        }
    
    override suspend fun refreshNfts(ownerAddress: String) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Refreshing NFTs for owner: $ownerAddress")
            
            supervisorScope {
                val allNfts = SUPPORTED_CHAINS.mapNotNull { chain ->
                    val chainId = chain.chainId
                    val networkName = chainIdToName(chainId)
                    
                    if (networkName.isBlank()) {
                        Log.w(TAG, "Unsupported chain: $chainId")
                        return@mapNotNull null
                    }
                    
                    async {
                        fetchNftsForChain(ownerAddress, chainId, networkName)
                    }
                }.awaitAll().flatten()
                
                if (allNfts.isNotEmpty()) {
                    // Clear old NFTs and insert new ones
                    nftDao.deleteAllNfts()
                    nftDao.upsertNfts(allNfts.map(NFT::asEntity))
                    Log.d(TAG, "Saved ${allNfts.size} NFTs to database")
                } else {
                    Log.d(TAG, "No NFTs found")
                    nftDao.deleteAllNfts()
                }
            }
        }
    }
    
    override suspend fun refreshNftsByChain(ownerAddress: String, chainId: Int) {
        withContext(Dispatchers.IO) {
            val networkName = chainIdToName(chainId)
            if (networkName.isBlank()) {
                Log.w(TAG, "Unsupported chain: $chainId")
                return@withContext
            }
            
            val nfts = fetchNftsForChain(ownerAddress, chainId, networkName)
            
            // Clear old NFTs for this chain and insert new ones
            nftDao.deleteNftsByChain(chainId)
            if (nfts.isNotEmpty()) {
                nftDao.upsertNfts(nfts.map(NFT::asEntity))
            }
        }
    }
    
    override suspend fun getNft(contractAddress: String, tokenId: String, chainId: Int): NFT? {
        return nftDao.getNft(contractAddress, tokenId, chainId)?.asDomainModel()
    }
    
    override fun observeNftsExist(): Flow<Boolean> = nftDao.observeNftsExist()
    
    override suspend fun removeNft(contractAddress: String, tokenId: String, chainId: Int) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Removing NFT: $contractAddress/$tokenId on chain $chainId")
            nftDao.deleteNft(contractAddress, tokenId, chainId)
        }
    }
    
    override suspend fun updateNftBalance(contractAddress: String, tokenId: String, chainId: Int, newBalance: Int) {
        withContext(Dispatchers.IO) {
            if (newBalance <= 0) {
                Log.d(TAG, "Removing NFT with zero balance: $contractAddress/$tokenId on chain $chainId")
                nftDao.deleteNft(contractAddress, tokenId, chainId)
            } else {
                Log.d(TAG, "Updating NFT balance to $newBalance: $contractAddress/$tokenId on chain $chainId")
                nftDao.updateNftBalance(contractAddress, tokenId, chainId, newBalance)
            }
        }
    }
    
    override suspend fun isNftSoulbound(contractAddress: String, tokenId: String, chainId: Int): Boolean {
        return soulboundChecker.isNftSoulbound(chainId, contractAddress, tokenId)
    }
    
    private suspend fun fetchNftsForChain(
        ownerAddress: String,
        chainId: Int,
        networkName: String
    ): List<NFT> {
        return try {
            val apiKey = BuildConfig.ALCHEMY_API
            val url = "https://$networkName.g.alchemy.com/nft/v3/$apiKey/getNFTsForOwner"
            
            Log.d(TAG, "Fetching NFTs from: $url for chain $chainId")
            
            val allNfts = mutableListOf<NFT>()
            var pageKey: String? = null
            
            // Fetch all pages
            do {
                val response = nftApi.getNFTsForOwner(
                    url = url,
                    owner = ownerAddress,
                    pageKey = pageKey
                )
                
                val nfts = response.ownedNfts.map { it.toDomainModel(chainId) }
                allNfts.addAll(nfts)
                
                pageKey = response.pageKey
                Log.d(TAG, "Fetched ${nfts.size} NFTs, total: ${allNfts.size}, hasMore: ${pageKey != null}")
                
            } while (pageKey != null)
            
            Log.d(TAG, "Total NFTs for chain $chainId: ${allNfts.size}")
            allNfts
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching NFTs for chain $chainId", e)
            emptyList()
        }
    }
}
