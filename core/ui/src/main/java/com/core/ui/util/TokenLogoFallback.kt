package com.core.ui.util

import com.core.ui.R

/**
 * Fallback system for token logos when API doesn't provide logo URLs.
 * Supports both remote URLs and local drawable resources.
 * 
 * Usage examples:
 * 
 * // Add a new token logo URL fallback
 * TokenLogoFallback.addUrlFallback("SHIB", "https://static.alchemyapi.io/images/assets/5994.png")
 * 
 * // Add a local drawable resource fallback
 * TokenLogoFallback.addResourceFallback("CUSTOM", R.drawable.custom_token_logo)
 * 
 * // Check if a token has a fallback
 * if (TokenLogoFallback.hasFallback("AAVE")) {
 *     val logo = TokenLogoFallback.getFallbackLogo("AAVE")
 *     // Use the logo...
 * }
 * 
 * // Remove a fallback
 * TokenLogoFallback.removeFallback("OLD_TOKEN")
 */
object TokenLogoFallback {
    
    /**
     * Sealed class to represent different types of logo sources
     */
    sealed class LogoSource {
        data class Url(val url: String) : LogoSource()
        data class LocalResource(val resourceId: Int) : LogoSource()
    }
    
    /**
     * Map of token symbols to their fallback logo sources.
     * Key: Token symbol (uppercase)
     * Value: LogoSource (either URL or local resource)
     */
    private val fallbackLogos = mutableMapOf<String, LogoSource>(
        // Add AAVE with the Alchemy static URL
        "AAVE" to LogoSource.Url("https://static.alchemyapi.io/images/assets/7278.png"),
        
        // Add other common tokens that might have missing logos
        "USDC" to LogoSource.Url("https://static.alchemyapi.io/images/assets/3408.png"),
        "USDT" to LogoSource.Url("https://static.alchemyapi.io/images/assets/825.png"),
        "DAI" to LogoSource.Url("https://static.alchemyapi.io/images/assets/4943.png"),
        "WBTC" to LogoSource.Url("https://static.alchemyapi.io/images/assets/3717.png"),
        "LINK" to LogoSource.Url("https://static.alchemyapi.io/images/assets/1975.png"),
        "UNI" to LogoSource.Url("https://static.alchemyapi.io/images/assets/7083.png"),
        "MATIC" to LogoSource.Url("https://static.alchemyapi.io/images/assets/3890.png"),
        "CRV" to LogoSource.Url("https://static.alchemyapi.io/images/assets/6538.png"),
        "SNX" to LogoSource.Url("https://static.alchemyapi.io/images/assets/2586.png"),
        "COMP" to LogoSource.Url("https://static.alchemyapi.io/images/assets/5692.png"),
        "MKR" to LogoSource.Url("https://static.alchemyapi.io/images/assets/1518.png"),
        
        // Native tokens with local resources (already in the app)
        "ETH" to LogoSource.LocalResource(R.drawable.ethereum_placeholder)
        // Note: Add local drawable resources for these tokens when available:
        // "MAINNET" to LogoSource.LocalResource(R.drawable.mainnet),
        // "OPTIMISM" to LogoSource.LocalResource(R.drawable.optimism),
        // "ARBITRUM" to LogoSource.LocalResource(R.drawable.arbitrum),
        // "POLYGON" to LogoSource.LocalResource(R.drawable.polygon),
        // "SEPOLIA" to LogoSource.LocalResource(R.drawable.sepolia),
        // "BASE" to LogoSource.LocalResource(R.drawable.base),
        // "ZORA" to LogoSource.LocalResource(R.drawable.zora)
    )
    
    /**
     * Get fallback logo for a token symbol
     * @param symbol Token symbol (case insensitive)
     * @return LogoSource if available, null otherwise
     */
    fun getFallbackLogo(symbol: String): LogoSource? {
        return fallbackLogos[symbol.uppercase()]
    }
    
    /**
     * Add or update a fallback logo URL
     * @param symbol Token symbol
     * @param url Logo URL
     */
    fun addUrlFallback(symbol: String, url: String) {
        fallbackLogos[symbol.uppercase()] = LogoSource.Url(url)
    }
    
    /**
     * Add or update a fallback logo from local resources
     * @param symbol Token symbol  
     * @param resourceId Drawable resource ID
     */
    fun addResourceFallback(symbol: String, resourceId: Int) {
        fallbackLogos[symbol.uppercase()] = LogoSource.LocalResource(resourceId)
    }
    
    /**
     * Remove a fallback logo
     * @param symbol Token symbol
     */
    fun removeFallback(symbol: String) {
        fallbackLogos.remove(symbol.uppercase())
    }
    
    /**
     * Get all registered fallback tokens
     * @return Set of token symbols that have fallbacks
     */
    fun getAllFallbackTokens(): Set<String> {
        return fallbackLogos.keys.toSet()
    }
    
    /**
     * Clear all fallback logos
     */
    fun clearAll() {
        fallbackLogos.clear()
    }
    
    /**
     * Check if a token has a fallback logo
     * @param symbol Token symbol
     * @return true if fallback exists
     */
    fun hasFallback(symbol: String): Boolean {
        return fallbackLogos.containsKey(symbol.uppercase())
    }
} 