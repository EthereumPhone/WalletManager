package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata
import java.math.BigDecimal

@Dao
interface TokenMetadataDao {
    @Query("SELECT * FROM token_metadata")
    fun getTokensMetadata(): List<TokenMetadata>

    @Query("""
        SELECT * FROM token_metadata
        WHERE symbol == :symbol
        """)
    fun getTokenMetadataBySymbol(symbol: String): TokenMetadata

    @Query("""
        SELECT * FROM token_metadata
        WHERE decimals == :decimals
        """)
    fun getTokenMetadataByDecimals(decimals: Int): TokenMetadata

    @Query("""
        SELECT * FROM token_metadata
        WHERE name == :name
        """)
    fun getTokenMetadataByName(name: String): TokenMetadata

    @Insert
    fun insertAllTokens(entities: List<TokenMetadata>)

}