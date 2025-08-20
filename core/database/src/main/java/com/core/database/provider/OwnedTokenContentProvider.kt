package com.core.database.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.dao.TokenExchangeDao
import com.core.ui.util.TokenLogoFallback
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

class OwnedTokenContentProvider : ContentProvider() {

    companion object {
        private const val TOKEN_BY_CHAIN_AND_ADDRESS = 1
        private const val TOKENS_BY_CHAIN = 2
        private const val TOKENS_ALL = 3

        const val COLUMN_CONTRACT_ADDRESS = "contract_address"
        const val COLUMN_DECIMALS = "decimals"
        const val COLUMN_NAME = "name"
        const val COLUMN_SYMBOL = "symbol"
        const val COLUMN_LOGO = "logo"
        const val COLUMN_CHAIN_ID = "chain_id"
        const val COLUMN_SWAPPABLE = "swappable"
        const val COLUMN_BALANCE = "balance"
        const val COLUMN_PRICE = "price"
        const val COLUMN_CHAINS = "chains"

        val COLUMNS = arrayOf(
            COLUMN_CONTRACT_ADDRESS,
            COLUMN_DECIMALS,
            COLUMN_NAME,
            COLUMN_SYMBOL,
            COLUMN_LOGO,
            COLUMN_CHAIN_ID,
            COLUMN_SWAPPABLE,
            COLUMN_BALANCE,
            COLUMN_PRICE,
            COLUMN_CHAINS
        )
    }

    private lateinit var authority: String
    private lateinit var uriMatcher: UriMatcher

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface OwnedTokenContentProviderEntryPoint {
        fun tokenMetadataDao(): TokenMetadataDao
        fun tokenBalanceDao(): TokenBalanceDao
        fun tokenExchangeDao(): TokenExchangeDao
    }

    private lateinit var tokenMetadataDao: TokenMetadataDao
    private lateinit var tokenBalanceDao: TokenBalanceDao
    private lateinit var tokenExchangeDao: TokenExchangeDao

    /**
     * Get the effective logo URL for a token, checking fallback if needed
     * @param originalLogo The logo URL from the database
     * @param symbol The token symbol for fallback lookup
     * @return The effective logo URL (original, fallback, or special URI for local resources)
     */
    private fun getEffectiveLogo(originalLogo: String?, symbol: String): String? {
        // If we have a valid logo URL from the database, use it
        if (!originalLogo.isNullOrEmpty()) {
            return originalLogo
        }
        
        // Otherwise, check for fallback
        val fallbackLogo = TokenLogoFallback.getFallbackLogo(symbol)
        return when (fallbackLogo) {
            is TokenLogoFallback.LogoSource.Url -> fallbackLogo.url
            is TokenLogoFallback.LogoSource.LocalResource -> {
                // For local resources, return a special URI that consuming apps can recognize
                // Format: android.resource://packageName/resourceId
                "android.resource://${context?.packageName}/${fallbackLogo.resourceId}"
            }
            null -> null
        }
    }

    override fun onCreate(): Boolean {
        val ctx = context ?: return false

        authority = ctx.packageName + ".ownedtokens.provider"

        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(authority, "ownedToken/#/*", TOKEN_BY_CHAIN_AND_ADDRESS)
            addURI(authority, "ownedTokens/#", TOKENS_BY_CHAIN)
            addURI(authority, "ownedTokens", TOKENS_ALL)
        }

        val entryPoint = EntryPointAccessors.fromApplication(
            ctx.applicationContext,
            OwnedTokenContentProviderEntryPoint::class.java
        )
        tokenMetadataDao = entryPoint.tokenMetadataDao()
        tokenBalanceDao = entryPoint.tokenBalanceDao()
        tokenExchangeDao = entryPoint.tokenExchangeDao()

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = MatrixCursor(projection ?: COLUMNS)

        when (uriMatcher.match(uri)) {
            TOKEN_BY_CHAIN_AND_ADDRESS -> handleSingleToken(uri, cursor)
            TOKENS_BY_CHAIN -> handleTokensByChain(uri, cursor)
            TOKENS_ALL -> handleAllTokens(cursor)
            else -> return null
        }

        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    private fun handleSingleToken(uri: Uri, cursor: MatrixCursor) {
        val chainId = uri.pathSegments[1].toIntOrNull() ?: return
        val contractAddress = uri.pathSegments[2]

        runBlocking {
            val balances = tokenBalanceDao.getTokenBalances(listOf(contractAddress)).first()
            val balanceEntity = balances.firstOrNull { it.chainId == chainId && it.tokenBalance > BigDecimal.ZERO }
            if (balanceEntity != null) {
                val metadataList = tokenMetadataDao.getTokenMetadata(listOf(contractAddress)).first()
                val meta = metadataList.firstOrNull { it.chainId == chainId } ?: return@runBlocking

                addRow(cursor, balanceEntity.tokenBalance, meta)
            }
        }
    }

    private fun handleTokensByChain(uri: Uri, cursor: MatrixCursor) {
        val chainId = uri.pathSegments[1].toIntOrNull() ?: return

        runBlocking {
            val balances = tokenBalanceDao.getTokenBalances(chainId).first()
                .filter { it.tokenBalance > BigDecimal.ZERO }
            if (balances.isEmpty()) return@runBlocking

            val contractAddresses = balances.map { it.contractAddress }
            val metadataMap = tokenMetadataDao.getTokenMetadata(contractAddresses).first()
                .associateBy { it.contractAddress.lowercase() }

            balances.forEach { balanceEntity ->
                val meta = metadataMap[balanceEntity.contractAddress.lowercase()] ?: return@forEach
                addRow(cursor, balanceEntity.tokenBalance, meta)
            }
        }
    }

    private fun handleAllTokens(cursor: MatrixCursor) {
        runBlocking {
            val balances = tokenBalanceDao.getTokenBalances().first()
                .filter { it.tokenBalance > BigDecimal.ZERO }
            if (balances.isEmpty()) return@runBlocking

            val contractAddresses = balances.map { it.contractAddress }
            val metadataMap = tokenMetadataDao.getTokenMetadata(contractAddresses).first()
                .associateBy { it.contractAddress.lowercase() }

            balances.forEach { balanceEntity ->
                val meta = metadataMap[balanceEntity.contractAddress.lowercase()] ?: return@forEach
                addRow(cursor, balanceEntity.tokenBalance, meta)
            }
        }
    }

    private fun addRow(cursor: MatrixCursor, rawBalance: BigDecimal, meta: com.core.database.model.erc20.TokenMetadataEntity) {
        val displayBalance = rawBalance.movePointLeft(meta.decimals)
        val latestExchange = runBlocking { tokenExchangeDao.getLatestExchange(meta.symbol).first() }
        val priceValue = latestExchange?.value ?: 0.0
        val chainsList = computeChains(meta.symbol)

        cursor.addRow(
            arrayOf(
                meta.contractAddress,
                meta.decimals,
                meta.name,
                meta.symbol,
                getEffectiveLogo(meta.logo, meta.symbol),
                meta.chainId,
                if (meta.swappable) 1 else 0,
                displayBalance.toPlainString(),
                priceValue,
                chainsList
            )
        )
    }

    private fun computeChains(symbol: String): String = runBlocking {
        val metas = tokenMetadataDao.getTokensMetadata(symbol).first()
        if (metas.isEmpty()) return@runBlocking ""
        val addresses = metas.map { it.contractAddress }
        val balances = tokenBalanceDao.getTokenBalances(addresses).first()
        val chainIds = balances.filter { it.tokenBalance > BigDecimal.ZERO }.map { it.chainId }.distinct()
        chainIds.joinToString(",")
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        TOKEN_BY_CHAIN_AND_ADDRESS -> "vnd.android.cursor.item/vnd.$authority.ownedtoken"
        TOKENS_BY_CHAIN, TOKENS_ALL -> "vnd.android.cursor.dir/vnd.$authority.ownedtoken"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
} 