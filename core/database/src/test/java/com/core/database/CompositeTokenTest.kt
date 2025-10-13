package com.core.database

import com.core.database.model.erc20.CompositeToken
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.model.erc20.toExternalModel
import com.core.database.model.erc20.toExternalModelWithPrice
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

/**
 * Unit tests for CompositeToken to verify behavior with and without metadata.
 */
class CompositeTokenTest {

    // Test data
    private val testAddress = "0x1234567890abcdef1234567890abcdef12345678"
    private val testChainId = 1
    private val testBalance = BigDecimal("1500000000000000000") // 1.5 tokens with 18 decimals

    /**
     * Test Case 1: Token with complete metadata - should work normally
     */
    @Test
    fun tokenWithCompleteMetadata_shouldWorkNormally() {
        // Given: A token with complete metadata
        val metadata = TokenMetadataEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            symbol = "USDC",
            name = "USD Coin",
            decimals = 6,
            logo = "https://example.com/usdc.png",
            swappable = true,
            groupId = "usdc-group"
        )
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = BigDecimal("1500000") // 1.5 USDC with 6 decimals
        )
        val compositeToken = CompositeToken(metadata, balance)

        // When: Converting to external model
        val externalModel = compositeToken.toExternalModel()

        // Then: All metadata should be preserved
        assertEquals(testAddress, externalModel.address)
        assertEquals(testChainId, externalModel.chainId)
        assertEquals("USDC", externalModel.symbol)
        assertEquals("USD Coin", externalModel.name)
        assertEquals(6, externalModel.decimals)
        assertEquals("https://example.com/usdc.png", externalModel.logoUrl)
        assertTrue(externalModel.swappable)
        assertEquals(1.5, externalModel.balance, 0.001)
    }

    /**
     * Test Case 2: Token with balance but NO metadata - should use fallback values
     */
    @Test
    fun tokenWithoutMetadata_shouldUseFallbackValues() {
        // Given: A token with balance but NO metadata
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = testBalance
        )
        val compositeToken = CompositeToken(
            tokenBalanceEntity = balance,
            tokenMetadataEntity = null // No metadata!
        )

        // When: Converting to external model
        val externalModel = compositeToken.toExternalModel()

        // Then: Should use fallback values
        assertEquals(testAddress, externalModel.address)
        assertEquals(testChainId, externalModel.chainId)
        assertEquals("0x1234...", externalModel.symbol) // Shortened address
        assertEquals("0x1234...", externalModel.name)
        assertEquals(18, externalModel.decimals) // Default decimals
        assertNull(externalModel.logoUrl)
        assertFalse(externalModel.swappable) // Safety: don't allow swapping unknown tokens
        assertEquals(1.5, externalModel.balance, 0.001) // Should calculate with default decimals
    }

    /**
     * Test Case 3: Token without metadata should still have correct balance calculation
     */
    @Test
    fun tokenWithoutMetadata_shouldCalculateBalanceCorrectly() {
        // Given: A token with various balances but no metadata
        val testCases = listOf(
            BigDecimal("1000000000000000000") to 1.0,   // 1 token
            BigDecimal("500000000000000000") to 0.5,    // 0.5 tokens
            BigDecimal("2500000000000000000") to 2.5,   // 2.5 tokens
            BigDecimal("0") to 0.0                       // 0 tokens
        )

        testCases.forEach { (rawBalance, expectedBalance) ->
            val balance = TokenBalanceEntity(
                contractAddress = testAddress,
                chainId = testChainId,
                tokenBalance = rawBalance
            )
            val compositeToken = CompositeToken(null, balance)
            val externalModel = compositeToken.toExternalModel()

            assertEquals(
                "Failed for balance: $rawBalance",
                expectedBalance,
                externalModel.balance,
                0.001
            )
        }
    }

    /**
     * Test Case 4: Token with metadata and custom decimals
     */
    @Test
    fun tokenWithCustomDecimals_shouldCalculateCorrectly() {
        // Given: A token with 8 decimals (like WBTC)
        val metadata = TokenMetadataEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            symbol = "WBTC",
            name = "Wrapped Bitcoin",
            decimals = 8,
            logo = null,
            swappable = true,
            groupId = null
        )
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = BigDecimal("50000000") // 0.5 WBTC
        )
        val compositeToken = CompositeToken(metadata, balance)
        val externalModel = compositeToken.toExternalModel()

        assertEquals(0.5, externalModel.balance, 0.001)
        assertEquals(8, externalModel.decimals)
    }

    /**
     * Test Case 5: Test getBalanceInUsd with metadata
     */
    @Test
    fun getBalanceInUsd_withMetadata_shouldCalculateCorrectly() {
        // Given: A token with metadata and balance
        val metadata = TokenMetadataEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            symbol = "ETH",
            name = "Ethereum",
            decimals = 18,
            logo = null,
            swappable = true,
            groupId = null
        )
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = BigDecimal("2000000000000000000") // 2 ETH
        )
        val compositeToken = CompositeToken(metadata, balance)

        // When: Calculating USD value with exchange rate of $2000/ETH
        val usdValue = compositeToken.getBalanceInUsd(2000.0, "USD")

        // Then: Should be 2 ETH * $2000 = $4000
        assertNotNull(usdValue)
        assertEquals(4000.0, usdValue!!, 0.01)
    }

    /**
     * Test Case 6: Test getBalanceInUsd without metadata (uses default 18 decimals)
     */
    @Test
    fun getBalanceInUsd_withoutMetadata_shouldUseDefaultDecimals() {
        // Given: A token without metadata
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = BigDecimal("1000000000000000000") // 1 token (18 decimals)
        )
        val compositeToken = CompositeToken(null, balance)

        // When: Calculating USD value with exchange rate of $100
        val usdValue = compositeToken.getBalanceInUsd(100.0, "USD")

        // Then: Should be 1 token * $100 = $100
        assertNotNull(usdValue)
        assertEquals(100.0, usdValue!!, 0.01)
    }

    /**
     * Test Case 7: Test toExternalModelWithPrice without metadata
     */
    @Test
    fun toExternalModelWithPrice_withoutMetadata_shouldHaveZeroFiatAmount() {
        // Given: A token without metadata
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = testBalance
        )
        val compositeToken = CompositeToken(null, balance)

        // When: Converting to external model with price
        val externalModel = compositeToken.toExternalModelWithPrice()

        // Then: Should have fallback values and zero fiat amount
        assertEquals(testAddress, externalModel.address)
        assertEquals("0x1234...", externalModel.symbol)
        assertEquals("0x1234...", externalModel.name) // Uses shortened address
        assertEquals(0.0, externalModel.fiatAmount, 0.001) // No price data in base model
    }

    /**
     * Test Case 8: Test contractAddress property fallback
     */
    @Test
    fun contractAddress_shouldFallbackToBalanceEntity() {
        // Given: Token without metadata
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = testBalance
        )
        val compositeToken = CompositeToken(null, balance)

        // When/Then: Should get address from balance entity
        assertEquals(testAddress, compositeToken.contractAddress)
    }

    /**
     * Test Case 9: Test chainId property fallback
     */
    @Test
    fun chainId_shouldFallbackToBalanceEntity() {
        // Given: Token without metadata
        val balance = TokenBalanceEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            tokenBalance = testBalance
        )
        val compositeToken = CompositeToken(null, balance)

        // When/Then: Should get chainId from balance entity
        assertEquals(testChainId, compositeToken.chainId)
    }

    /**
     * Test Case 10: Token with null balance should return 0.0
     */
    @Test
    fun tokenWithNullBalance_shouldReturnZeroBalance() {
        // Given: Token with metadata but no balance
        val metadata = TokenMetadataEntity(
            contractAddress = testAddress,
            chainId = testChainId,
            symbol = "TEST",
            name = "Test Token",
            decimals = 18,
            logo = null,
            swappable = true,
            groupId = null
        )
        val compositeToken = CompositeToken(metadata, null)

        // When: Converting to external model
        val externalModel = compositeToken.toExternalModel()

        // Then: Balance should be 0.0
        assertEquals(0.0, externalModel.balance, 0.001)
    }

    /**
     * Test Case 11: Token with minimal data (balance only, empty address)
     */
    @Test
    fun tokenWithEmptyAddress_shouldHandleGracefully() {
        // Given: Token with balance but empty address
        val balance = TokenBalanceEntity(
            contractAddress = "",
            chainId = 0,
            tokenBalance = BigDecimal.ZERO
        )
        val compositeToken = CompositeToken(null, balance)

        // When: Converting to external model
        val externalModel = compositeToken.toExternalModel()

        // Then: Should have fallback values
        assertEquals("", externalModel.address)
        assertEquals(0, externalModel.chainId)
        assertEquals("...", externalModel.symbol) // Empty address produces "..."
        assertEquals(0.0, externalModel.balance, 0.001)
    }

    /**
     * Test Case 12: Verify shortened address format
     */
    @Test
    fun shortenedAddress_shouldHaveCorrectFormat() {
        // Given: Various addresses
        val testAddresses = listOf(
            "0xabcdef1234567890" to "0xabcd...",
            "0x123456" to "0x1234...",
            "0x12" to "0x12...",
            "" to "..."
        )

        testAddresses.forEach { (address, expectedSymbol) ->
            val balance = TokenBalanceEntity(
                contractAddress = address,
                chainId = 1,
                tokenBalance = BigDecimal.ZERO
            )
            val compositeToken = CompositeToken(null, balance)
            val externalModel = compositeToken.toExternalModel()

            assertEquals(
                "Failed for address: $address",
                expectedSymbol,
                externalModel.symbol
            )
        }
    }
}

