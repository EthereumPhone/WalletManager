package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.core.database.model.NftEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for NFT operations
 */
@Dao
interface NftDao {
    
    /**
     * Get all NFTs as a Flow
     */
    @Query("SELECT * FROM nfts ORDER BY collectionName ASC, name ASC")
    fun getAllNfts(): Flow<List<NftEntity>>
    
    /**
     * Get NFTs for a specific chain
     */
    @Query("SELECT * FROM nfts WHERE chainId = :chainId ORDER BY collectionName ASC, name ASC")
    fun getNftsByChain(chainId: Int): Flow<List<NftEntity>>
    
    /**
     * Get a specific NFT by contract address and token ID
     */
    @Query("SELECT * FROM nfts WHERE contractAddress = :contractAddress AND tokenId = :tokenId AND chainId = :chainId LIMIT 1")
    suspend fun getNft(contractAddress: String, tokenId: String, chainId: Int): NftEntity?
    
    /**
     * Insert or update NFTs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNfts(nfts: List<NftEntity>)
    
    /**
     * Delete all NFTs (used before refresh)
     */
    @Query("DELETE FROM nfts")
    suspend fun deleteAllNfts()
    
    /**
     * Delete NFTs for a specific chain
     */
    @Query("DELETE FROM nfts WHERE chainId = :chainId")
    suspend fun deleteNftsByChain(chainId: Int)
    
    /**
     * Check if any NFTs exist
     */
    @Query("SELECT EXISTS(SELECT 1 FROM nfts LIMIT 1)")
    fun observeNftsExist(): Flow<Boolean>
    
    /**
     * Get total count of NFTs
     */
    @Query("SELECT COUNT(*) FROM nfts")
    suspend fun getNftCount(): Int
    
    /**
     * Delete a specific NFT
     */
    @Query("DELETE FROM nfts WHERE contractAddress = :contractAddress AND tokenId = :tokenId AND chainId = :chainId")
    suspend fun deleteNft(contractAddress: String, tokenId: String, chainId: Int)
    
    /**
     * Update NFT balance (for ERC1155 tokens)
     */
    @Query("UPDATE nfts SET balance = :newBalance WHERE contractAddress = :contractAddress AND tokenId = :tokenId AND chainId = :chainId")
    suspend fun updateNftBalance(contractAddress: String, tokenId: String, chainId: Int, newBalance: Int)
}
