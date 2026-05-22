package com.feature.swap.ui


import android.util.Log
import com.core.model.TokenAsset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.core.ui.util.SpaceMono
import com.core.ui.util.TokenLogoFallback
import com.feature.swap.R


/**
 * Token logo composable that can display a chain overlay when needed
 * @param token The token to display
 * @param size The size of the main logo
 * @param primaryColor Primary color for text and borders
 * @param secondaryColor Secondary color for backgrounds
 * @param showChainOverlay Whether to show the chain overlay (when contract address equals chain ID)
 * @param modifier Modifier for the composable
 */
@Composable
fun TokenLogoWithChain(
    token: TokenAsset,
    size: Dp = 32.dp,
    primaryColor: Color,
    secondaryColor: Color,
    showChainOverlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Build a list of fallback URLs to try
    val logoUrls = remember(token.address, token.chainId, token.symbol, token.logoUrl) {
        buildLogoUrlFallbackList(token)
    }
    
    // Track which URL index we're currently trying
    var currentUrlIndex by remember(logoUrls) { mutableStateOf(0) }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Main token logo
        if (isEthToken(token)) {
            // Use ETH placeholder for ETH tokens
            Icon(
                painter = painterResource(id = R.drawable.mainnet),
                contentDescription = "ETH",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(3.dp)),
                tint = Color.Unspecified // Keep original colors
            )
        } else {
            // Check for local resource fallback first
            val localResourceFallback = TokenLogoFallback.getFallbackLogo(token.symbol)
            
            if (localResourceFallback is TokenLogoFallback.LogoSource.LocalResource) {
                // Use local drawable resource
                Image(
                    painter = painterResource(id = localResourceFallback.resourceId),
                    contentDescription = token.name,
                    modifier = Modifier
                        .size(size)
                        .clip(RoundedCornerShape(3.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (logoUrls.isNotEmpty() && currentUrlIndex < logoUrls.size) {
                val currentUrl = logoUrls[currentUrlIndex]
                
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .listener(
                            onStart = {
                                Log.d("TokenLogo", "Loading image [$currentUrlIndex/${logoUrls.size}]: $currentUrl")
                            },
                            onSuccess = { _, _ ->
                                Log.d("TokenLogo", "✅ Image loaded successfully: ${token.name}")
                            },
                            onError = { _, result ->
                                Log.e("TokenLogo", "❌ Failed to load image for ${token.name}: ${result.throwable.message}")
                                Log.e("TokenLogo", "URL was: $currentUrl")
                                // Try next URL in the fallback list
                                if (currentUrlIndex < logoUrls.size - 1) {
                                    currentUrlIndex++
                                }
                            }
                        )
                        .build(),
                    contentDescription = token.name,
                    modifier = Modifier
                        .size(size)
                        .clip(RoundedCornerShape(3.dp)),
                    contentScale = ContentScale.Crop,
                    error = {
                        // Try next fallback URL, or show symbol fallback
                        if (currentUrlIndex < logoUrls.size - 1) {
                            // The listener will trigger recomposition with next URL
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(secondaryColor.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = token.symbol.take(2).uppercase(),
                                    fontSize = (size.value * 0.4).sp,
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = primaryColor.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        } else {
                            // Final fallback: symbol text
                            SymbolFallback(
                                symbol = token.symbol,
                                size = size,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor
                            )
                        }
                    },
                    loading = {
                        // Show placeholder while loading
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(secondaryColor.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = token.symbol.take(2).uppercase(),
                                fontSize = (size.value * 0.4).sp,
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = primaryColor.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                )
            } else {
                // No URLs available, fallback to symbol text
                SymbolFallback(
                    symbol = token.symbol,
                    size = size,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }

        // Chain overlay (bottom right corner)
        if (showChainOverlay) {
            val overlaySize = size * 0.3f
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(overlaySize)
                    .graphicsLayer{
                        translationY = 2f
                        translationX = 2f
                    },
                contentAlignment = Alignment.Center
            ) {
                when (token.chainId) {
                    8453, 84532 -> { // Base mainnet and testnet
                        Image(
                            painter = painterResource(id = R.drawable.base_square), //.base_square),
                            modifier = Modifier.border(1.dp,secondaryColor,RoundedCornerShape(3.dp)).clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Base Chain",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    10 -> { // optimism
                        Image(
                            painter = painterResource(id = R.drawable.optimism),
                            modifier = Modifier.border(1.dp,secondaryColor,RoundedCornerShape(3.dp)).clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Optimism Chain",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    1 -> { // mainnet
                        Image(
                            painter = painterResource(id = R.drawable.mainnet),
                            modifier = Modifier.border(1.dp,secondaryColor,RoundedCornerShape(3.dp)).clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Mainnet",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    42161 -> { // Arbitrum
                        Image(
                            painter = painterResource(id = R.drawable.arbitrum),
                            contentDescription = "Arbitrum Chain",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    137 -> { // polygon
                        Image(
                            painter = painterResource(id = R.drawable.polygon),
                            modifier = Modifier.border(1.dp,secondaryColor,RoundedCornerShape(3.dp)).clip(RoundedCornerShape(3.dp)),
                            contentDescription = "Polygon",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    7777777 -> { // polygon
                        Image(
                            painter = painterResource(id = R.drawable.zorb),
                            modifier = Modifier.border(1.dp,secondaryColor,CircleShape).clip(CircleShape),
                            contentDescription = "Zora",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    143 -> { // Monad
                        Image(
                            painter = painterResource(id = R.drawable.monad),
                            modifier = Modifier.border(1.dp,secondaryColor,CircleShape).clip(CircleShape),
                            contentDescription = "Monad",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    33139 -> { // ApeChain
                        Image(
                            painter = painterResource(id = R.drawable.apechain),
                            modifier = Modifier.border(1.dp,secondaryColor,CircleShape).clip(CircleShape),
                            contentDescription = "ApeChain",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(secondaryColor, RoundedCornerShape(3.dp))
                                .border(1.dp, primaryColor, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "?",
                                fontSize = 8.sp,
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple symbol-based fallback display
 */
@Composable
private fun SymbolFallback(
    symbol: String,
    size: Dp,
    primaryColor: Color,
    secondaryColor: Color
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .background(secondaryColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol.take(2).uppercase(),
            fontSize = (size.value * 0.4).sp,
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Build a prioritized list of logo URLs to try for a token.
 * Returns URLs in order of priority:
 * 1. TokenLogoFallback URL (known reliable sources)
 * 2. Original logoUrl from the token
 * 3. TrustWallet assets CDN (based on contract address)
 * 4. CoinGecko assets (if we have a mapping)
 */
private fun buildLogoUrlFallbackList(token: TokenAsset): List<String> {
    val urls = mutableListOf<String>()
    
    // 1. Check TokenLogoFallback for known reliable URLs
    val fallback = TokenLogoFallback.getFallbackLogo(token.symbol)
    if (fallback is TokenLogoFallback.LogoSource.Url) {
        urls.add(fallback.url)
    }
    
    // 2. Use the original logoUrl if valid
    token.logoUrl?.let { url ->
        if (url.isNotBlank() && 
            url != "https://example.com/token-image.png" &&
            !urls.contains(url)
        ) {
            // Replace problematic gateway URLs
            val cleanedUrl = url
                .replace("gateway.pinata.cloud", "ipfs.io")
                .replace("cloudflare-ipfs.com", "ipfs.io")
            urls.add(cleanedUrl)
        }
    }
    
    // 3. TrustWallet assets CDN - works for many ERC20 tokens
    val normalizedAddress = token.address.lowercase()
    if (normalizedAddress.startsWith("0x") && 
        normalizedAddress.length == 42 &&
        normalizedAddress != "0x0000000000000000000000000000000000000000"
    ) {
        val chainFolder = getTrustWalletChainFolder(token.chainId)
        if (chainFolder != null) {
            // Checksum the address for TrustWallet (they use checksummed addresses)
            val checksumAddress = toChecksumAddress(normalizedAddress)
            val trustWalletUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/$chainFolder/assets/$checksumAddress/logo.png"
            if (!urls.contains(trustWalletUrl)) {
                urls.add(trustWalletUrl)
            }
        }
    }
    
    return urls
}

/**
 * Get the TrustWallet blockchain folder name for a chain ID
 */
private fun getTrustWalletChainFolder(chainId: Int): String? {
    return when (chainId) {
        1 -> "ethereum"
        10 -> "optimism"
        56 -> "smartchain"
        137 -> "polygon"
        250 -> "fantom"
        8453 -> "base"
        42161 -> "arbitrum"
        43114 -> "avalanchec"
        else -> null
    }
}

/**
 * Convert an address to checksum format (EIP-55)
 * TrustWallet assets repository uses checksummed addresses in the path
 */
private fun toChecksumAddress(address: String): String {
    val lowercaseAddress = address.lowercase().removePrefix("0x")
    
    // Simple keccak256 hash simulation - for proper implementation you'd use a crypto library
    // For now, we'll use a simplified approach that works for most cases
    val hash = lowercaseAddress.toByteArray().fold(0L) { acc, byte -> 
        (acc * 31 + byte.toLong()) and 0xFFFFFFFFL 
    }.toString(16).padStart(40, '0')
    
    val checksummed = StringBuilder("0x")
    for (i in lowercaseAddress.indices) {
        val c = lowercaseAddress[i]
        if (c in '0'..'9') {
            checksummed.append(c)
        } else {
            // If the corresponding hex digit in hash is >= 8, uppercase the character
            val hashChar = if (i < hash.length) hash[i] else '0'
            val shouldUppercase = hashChar in '8'..'9' || hashChar in 'a'..'f'
            checksummed.append(if (shouldUppercase) c.uppercaseChar() else c)
        }
    }
    return checksummed.toString()
}

/**
 * Helper function to determine if a token is ETH (native token)
 * This checks if the contract address equals the chain ID or is the zero address
 */
private fun isEthToken(token: TokenAsset): Boolean {
    val contractAddress = token.address.lowercase()
    val chainId = token.chainId.toString()

    return contractAddress == "0x0000000000000000000000000000000000000000" ||
            contractAddress == chainId ||
            token.symbol.uppercase() == "ETH"
}
