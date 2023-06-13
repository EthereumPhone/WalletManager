package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenMetadataEntity

@Dao
interface TokenMetadataDao {
    @Query("SELECT * FROM token_metadata")
    fun getTokensMetadata(): List<TokenMetadataEntity>

    @Query("""
        SELECT * FROM token_metadata
        WHERE symbol == :symbol
        """)
    fun getTokenMetadataBySymbol(symbol: String): TokenMetadataEntity

    @Query("""
        SELECT * FROM token_metadata
        WHERE decimals == :decimals
        """)
    fun getTokenMetadataByDecimals(decimals: Int): TokenMetadataEntity

    @Query("""
        SELECT * FROM token_metadata
        WHERE name == :name
        """)
    fun getTokenMetadataByName(name: String): TokenMetadataEntity

    @Insert
    fun insertAllTokens(entities: List<TokenMetadataEntity>)

}