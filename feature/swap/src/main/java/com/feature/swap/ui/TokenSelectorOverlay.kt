package com.feature.swap.ui

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.core.database.provider.TokenMetadataProviderContract
import com.core.model.TokenAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.NetworkChain
import com.core.model.TokenAssetWithPrice
import com.core.ui.DgenSearchBar
import com.core.ui.HeaderBar
import com.core.ui.InfoScreen
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite
import com.core.ui.util.formatWithSuffix
import com.core.ui.util.neonOpacity
import com.feature.swap.CustomTokenLookupState
import com.feature.swap.TokenSelectionMode


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TokenSelectorOverlay(
    isVisible: Boolean,
    mode: TokenSelectionMode,
    fromTokens: List<TokenAssetWithPrice>,
    toTokens: List<TokenAssetWithPrice>,
    selectFromToken: (TokenAssetWithPrice) -> Unit,
    selectToToken: (TokenAssetWithPrice) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onDismiss: () -> Unit,
    currentChainId: Int?,
    selectedChainId: Int,
    onChainSelected: (Int) -> Unit,
    groupedTokens: List<TokenGroupAssetOverview> = emptyList(),
    // Custom token lookup support (paste contract address feature)
    customTokenLookupState: CustomTokenLookupState = CustomTokenLookupState.Idle,
    onLookupCustomToken: (String, Int) -> Unit = { _, _ -> },
    onClearCustomTokenLookup: () -> Unit = {},
    isContractAddress: (String) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    // Search and filtering state
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    var context = LocalContext.current

    // Chain selector overlay state
    var isChainSelectorVisible by remember { mutableStateOf(false) }

    // Detect contract address paste and trigger lookup
    LaunchedEffect(searchQuery, selectedChainId) {
        if (isContractAddress(searchQuery)) {
            // Debounce the lookup slightly
            kotlinx.coroutines.delay(300)
            onLookupCustomToken(searchQuery, selectedChainId)
        } else {
            onClearCustomTokenLookup()
        }
    }

    // Clear custom token state when overlay closes
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            onClearCustomTokenLookup()
        }
    }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(dgenBlack)
        ) {

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (mode) {
                    TokenSelectionMode.From -> {
                        val context = LocalContext.current

                        LaunchedEffect(isVisible, groupedTokens) {
                            // Collect prices from all chains that the user has tokens on
                            val uniqueChainIds = fromTokens.map { it.chainId }.distinct()
                            val allPrices = mutableMapOf<String, Double>()
                            
                            // First, get unit price from grouped tokens for ETH
                            val ethGroup = groupedTokens.firstOrNull { 
                                it.symbol.equals("ETH", ignoreCase = true) || 
                                it.name.contains("Ethereum", ignoreCase = true)
                            }
                            
                            val ethUnitPrice = ethGroup?.let { group ->
                                val totalFiat = group.totalFiatBalance
                                if (group.totalBalance > 0 && totalFiat != null && totalFiat > 0) {
                                    val price = totalFiat / group.totalBalance
                                    android.util.Log.d("TokenSelector", "ETH Group Found - Total Balance: ${group.totalBalance}, Total Fiat: $totalFiat, Unit Price: $price")
                                    price
                                } else {
                                    android.util.Log.d("TokenSelector", "ETH Group found but invalid balance/fiat: balance=${group.totalBalance}, fiat=$totalFiat")
                                    null
                                }
                            } ?: run {
                                android.util.Log.d("TokenSelector", "ETH Group not found in grouped tokens")
                                null
                            }
                            
                            uniqueChainIds.forEach { chainId ->
                                runCatching {
                                    TokenMetadataProviderContract.getTokensByChain(
                                        context.contentResolver,
                                        chainId
                                    )
                                }.onSuccess { list ->
                                    list.forEach { token ->
                                        val address = token.contractAddress.lowercase()
                                        allPrices[address] = token.price
                                    }
                                }
                            }
                            
                            // Override ETH prices with the calculated unit price from grouped data
                            ethUnitPrice?.let { price ->
                                allPrices["0x0000000000000000000000000000000000000000"] = price
                                allPrices["0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"] = price
                                
                                // IMPORTANT: Network tokens (ETH) store their chain ID as the address
                                // So we need to map all ETH chain IDs to the same price
                                // Base, Optimism, Arbitrum, Mainnet, etc.
                                uniqueChainIds.forEach { chainId ->
                                    // Skip Polygon (137) as it uses MATIC, not ETH
                                    if (chainId != 137) {
                                        allPrices[chainId.toString()] = price
                                    }
                                }
                                
                                android.util.Log.d("TokenSelector", "Set ETH unit price to: $price for addresses and chain IDs: ${uniqueChainIds.filter { it != 137 }}")
                            }
                            
                            // Log all ETH tokens and their addresses
                            fromTokens.filter { it.symbol.equals("ETH", ignoreCase = true) }.forEach { token ->
                                android.util.Log.d("TokenSelector", "ETH Token - Address: ${token.address}, Chain: ${token.chainId}, Balance: ${token.balance}")
                            }

                        }

                        // Filter tokens based on search query and selected chain
                        val filteredFromTokens = remember(fromTokens, searchQuery, selectedChainId) {
                            fromTokens.filter { token ->
                                val matchesSearch = searchQuery.isEmpty() || 
                                    token.name.contains(searchQuery, ignoreCase = true) || 
                                    token.symbol.contains(searchQuery, ignoreCase = true) ||
                                    token.address.contains(searchQuery, ignoreCase = true)
                                
                                val matchesChain = selectedChainId == null || token.chainId == selectedChainId
                                
                                matchesSearch && matchesChain
                            }
                        }
                        // Ensure unique items by address+chain to avoid duplicate LazyColumn keys
                        val dedupedFromTokens = remember(filteredFromTokens) {
                            filteredFromTokens.distinctBy { it.address.lowercase() + "_" + it.chainId }
                        }

                        // Sort by highest-dollar value owned (balance * unit price)
                        val sortedFromTokens = remember(dedupedFromTokens) {
                            dedupedFromTokens.sortedByDescending { token ->
                                token.fiatAmount
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Search bar
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            ) {
                                DgenSearchBar(
                                    searchValue = searchQuery,
                                    onSearchValueChange = { searchQuery = it },
                                    focusedSearch = isFocused,
                                    onFocusChanged = { isFocused = it },
                                    textColor = primaryColor,
                                    backgroundColor = secondaryColor,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    focusRequester = focusRequester,
                                    keyboardController = keyboardController,
                                    onClear = { searchQuery = "" },
                                    onNavigateBack = onDismiss,
                                    selectedChainId = selectedChainId,
                                    onNetworkClick = {
                                        isChainSelectorVisible = true
                                    }
                                )
                            }

                            Box {
                                if (sortedFromTokens.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            Spacer(Modifier.fillMaxWidth().height(8.dp))
                                        }
                                        
                                        items(sortedFromTokens, key = { it.address + "_" + it.chainId }) { token ->

                                            TokenRow(
                                                token = token,
                                                fiatAmount = token.fiatAmount,
                                                unitPriceUsd = 0.0, // TODO: Not needed anymore
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                                onClick = {
                                                    android.util.Log.d("TokenSelector_CLICK", """
                                                        |=== FROM TOKEN CLICKED ===
                                                        |Token Name: ${token.name}
                                                        |Symbol: ${token.symbol}
                                                        |Address: ${token.address}
                                                        |Address (lowercase): ${token.address.lowercase()}
                                                        |Chain ID: ${token.chainId}
                                                        |Balance: ${token.balance}
                                                        |Calculated USD Value: ${token.fiatAmount}
                                                        |Is Network Token (ETH): ${token.address == token.chainId.toString()}
                                                    """.trimMargin())
                                                    selectFromToken(token)
                                                    onDismiss()
                                                }
                                            )
                                        }

                                        item {
                                            Spacer(Modifier.fillMaxWidth().height(24.dp))
                                        }
                                    }
                                } else {
                                    InfoScreen(
                                        description = "No tokens available.",
                                        primaryColor = primaryColor
                                    )
                                }

                                // Top gradient fade
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .align(Alignment.TopCenter)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(dgenBlack, Color.Transparent)
                                            )
                                        ).zIndex(3f)
                                )

                                // Bottom gradient fade
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .align(Alignment.BottomCenter).background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, dgenBlack)
                                            )
                                        )
                                        .zIndex(3f)
                                )
                            }
                        }
                    }
                    TokenSelectionMode.To -> {
                        val context = LocalContext.current
                        var priceMap by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

                        LaunchedEffect(isVisible, currentChainId, groupedTokens) {
                            val chainId = currentChainId
                            
                            // Get unit price from grouped tokens for ETH
                            val ethGroup = groupedTokens.firstOrNull { 
                                it.symbol.equals("ETH", ignoreCase = true) || 
                                it.name.contains("Ethereum", ignoreCase = true)
                            }
                            
                            val ethUnitPrice = ethGroup?.let { group ->
                                val totalFiat = group.totalFiatBalance
                                if (group.totalBalance > 0 && totalFiat != null && totalFiat > 0) {
                                    totalFiat / group.totalBalance
                                } else {
                                    null
                                }
                            }
                            
                            if (chainId != null) {
                                runCatching {
                                    TokenMetadataProviderContract.getTokensByChain(
                                        context.contentResolver,
                                        chainId
                                    )
                                }.onSuccess { list ->
                                    val prices = mutableMapOf<String, Double>()
                                    
                                    list.forEach { token ->
                                        val address = token.contractAddress.lowercase()
                                        prices[address] = token.price
                                    }
                                    
                                    // Override ETH prices with the calculated unit price from grouped data
                                    ethUnitPrice?.let { price ->
                                        prices["0x0000000000000000000000000000000000000000"] = price
                                        prices["0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"] = price
                                        
                                        // IMPORTANT: Network tokens (ETH) store their chain ID as the address
                                        // Map the current chain ID to the ETH price
                                        if (chainId != 137) { // Skip Polygon as it uses MATIC
                                            prices[chainId.toString()] = price
                                        }
                                    }
                                    
                                    priceMap = prices
                                }.onFailure {
                                    priceMap = emptyMap()
                                }
                            } else {
                                priceMap = emptyMap()
                            }
                        }

                        // Filter tokens based on search query and selected chain
                        val filteredTokens = remember(toTokens, searchQuery, selectedChainId) {
                            toTokens.filter { token ->
                                val matchesSearch = searchQuery.isEmpty() || 
                                    token.name.contains(searchQuery, ignoreCase = true) || 
                                    token.symbol.contains(searchQuery, ignoreCase = true) ||
                                    token.address.contains(searchQuery, ignoreCase = true)
                                
                                val matchesChain = selectedChainId == null || token.chainId == selectedChainId
                                
                                matchesSearch && matchesChain
                            }
                        }
                        
                        // Separate owned and non-owned tokens
                        val ownedTokens = remember(filteredTokens) {
                            filteredTokens
                                .filter { it.balance > 0 }
                                .distinctBy { it.address.lowercase() + "_" + it.chainId }
                        }
                        val ownedTokensSorted = remember(ownedTokens) {
                            ownedTokens.sortedByDescending { token ->
                                token.fiatAmount
                            }
                        }
                        
                        val allTokens = remember(filteredTokens) {
                            filteredTokens
                                .filter { it.balance == 0.0 }
                                .distinctBy { it.address.lowercase() + "_" + it.chainId }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Search bar
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            )
                            {
                                DgenSearchBar(
                                    searchValue = searchQuery,
                                    onSearchValueChange = { searchQuery = it },
                                    focusedSearch = isFocused,
                                    onFocusChanged = { isFocused = it },
                                    textColor = primaryColor,
                                    backgroundColor = secondaryColor,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    focusRequester = focusRequester,
                                    keyboardController = keyboardController,
                                    onClear = { searchQuery = "" },
                                    onNavigateBack = onDismiss,
                                    selectedChainId = selectedChainId,
                                    onNetworkClick = {
                                        isChainSelectorVisible = true
                                    }
                                )
                            }

                            Box(

                            ){
                                // Show list if there are filtered tokens OR if custom token lookup is active
                                val showTokenList = filteredTokens.isNotEmpty() || customTokenLookupState !is CustomTokenLookupState.Idle
                                if (showTokenList) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            Spacer(Modifier.fillMaxWidth().height(8.dp))
                                        }

                                        // CUSTOM TOKEN section (when pasting a contract address)
                                        when (val lookupState = customTokenLookupState) {
                                            is CustomTokenLookupState.Loading -> {
                                                item {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "LOOKING UP TOKEN...",
                                                            fontFamily = SpaceMono,
                                                            color = primaryColor.copy(alpha = 0.7f),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            letterSpacing = 1.sp,
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        androidx.compose.material3.CircularProgressIndicator(
                                                            modifier = Modifier.size(24.dp),
                                                            color = primaryColor,
                                                            strokeWidth = 2.dp
                                                        )
                                                    }
                                                }
                                            }
                                            is CustomTokenLookupState.Found -> {
                                                item {
                                                    Text(
                                                        text = "CUSTOM TOKEN",
                                                        fontFamily = SpaceMono,
                                                        color = primaryColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        letterSpacing = 1.sp,
                                                    )
                                                }
                                                item {
                                                    TokenRow(
                                                        token = lookupState.token,
                                                        unitPriceUsd = 0.0,
                                                        fiatAmount = 0.0,
                                                        primaryColor = primaryColor,
                                                        secondaryColor = secondaryColor,
                                                        onClick = {
                                                            android.util.Log.d("TokenSelector_CLICK", "Custom token selected: ${lookupState.token.symbol}")
                                                            selectToToken(lookupState.token)
                                                            onDismiss()
                                                        }
                                                    )
                                                }
                                                item {
                                                    Spacer(Modifier.fillMaxWidth().height(8.dp))
                                                }
                                            }
                                            is CustomTokenLookupState.NotFound -> {
                                                item {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "TOKEN NOT FOUND",
                                                            fontFamily = SpaceMono,
                                                            color = dgenRed,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            letterSpacing = 1.sp,
                                                        )
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = "No token found at this address on the selected chain",
                                                            fontFamily = PitagonsSans,
                                                            color = primaryColor.copy(alpha = 0.6f),
                                                            fontSize = 12.sp,
                                                        )
                                                    }
                                                }
                                            }
                                            is CustomTokenLookupState.Error -> {
                                                item {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "LOOKUP ERROR",
                                                            fontFamily = SpaceMono,
                                                            color = dgenRed,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            letterSpacing = 1.sp,
                                                        )
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = lookupState.message,
                                                            fontFamily = PitagonsSans,
                                                            color = primaryColor.copy(alpha = 0.6f),
                                                            fontSize = 12.sp,
                                                        )
                                                    }
                                                }
                                            }
                                            CustomTokenLookupState.Idle -> {
                                                // Show nothing when idle
                                            }
                                        }

                                        // OWNED section
                                        if (ownedTokensSorted.isNotEmpty()) {
                                            item {
                                                Text(
                                                    text = "OWNED",
                                                    fontFamily = SpaceMono,
                                                    color = primaryColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    letterSpacing = 1.sp,
                                                )
                                            }
                                            items(ownedTokensSorted, key = { it.address + "_" + it.chainId + "_owned" }) { token ->
                                                // For network tokens (ETH), the address is the chain ID
                                                // Try lookup by address first, then by chain ID as fallback
                                                TokenRow(
                                                    token = token,
                                                    unitPriceUsd = 0.0, //TODO: REMOVE THIS
                                                    fiatAmount = token.fiatAmount,
                                                    primaryColor = primaryColor,
                                                    secondaryColor = secondaryColor,
                                                    onClick = {
                                                        android.util.Log.d("TokenSelector_CLICK", """
                                                            |=== TO TOKEN CLICKED (OWNED) ===
                                                            |Token Name: ${token.name}
                                                            |Symbol: ${token.symbol}
                                                            |Address: ${token.address}
                                                            |Address (lowercase): ${token.address.lowercase()}
                                                            |Chain ID: ${token.chainId}
                                                            |Balance: ${token.balance}
                                                            |Price in Map (by address): ${priceMap[token.address.lowercase()]}
                                                            |Price in Map (by chainId): ${priceMap[token.chainId.toString()]}
                                                            |Is Network Token (ETH): ${token.address == token.chainId.toString()}
                                                        """.trimMargin())
                                                        selectToToken(token)
                                                        onDismiss()
                                                    }
                                                )
                                            }
                                            
                                            item {
                                                Spacer(Modifier.fillMaxWidth().height(16.dp))
                                            }
                                        }
                                        
                                        // ALL section (non-owned tokens)
                                        if (allTokens.isNotEmpty()) {
                                            item {
                                                Text(
                                                    text = "ALL",
                                                    fontFamily = SpaceMono,
                                                    color = primaryColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    letterSpacing = 1.sp,
                                                )
                                            }
                                            items(allTokens, key = { it.address + "_" + it.chainId + "_all" }) { token ->
                                                // For network tokens (ETH), the address is the chain ID
                                                // Try lookup by address first, then by chain ID as fallback
                                                val unitPrice = priceMap[token.address.lowercase()] 
                                                    ?: priceMap[token.chainId.toString()] 
                                                    ?: 0.0
                                                TokenRow(
                                                    token = token,
                                                    unitPriceUsd = unitPrice,
                                                    primaryColor = primaryColor,
                                                    secondaryColor = secondaryColor,
                                                    onClick = {
                                                        android.util.Log.d("TokenSelector_CLICK", """
                                                            |=== TO TOKEN CLICKED (ALL) ===
                                                            |Token Name: ${token.name}
                                                            |Symbol: ${token.symbol}
                                                            |Address: ${token.address}
                                                            |Address (lowercase): ${token.address.lowercase()}
                                                            |Chain ID: ${token.chainId}
                                                            |Balance: ${token.balance}
                                                            |Unit Price USD: $unitPrice
                                                            |Calculated USD Value: ${token.balance * unitPrice}
                                                            |Price in Map (by address): ${priceMap[token.address.lowercase()]}
                                                            |Price in Map (by chainId): ${priceMap[token.chainId.toString()]}
                                                            |Is Network Token (ETH): ${token.address == token.chainId.toString()}
                                                        """.trimMargin())
                                                        selectToToken(token)
                                                        onDismiss()
                                                    }
                                                )
                                            }
                                        }
                                        
                                        item {
                                            Spacer(Modifier.fillMaxWidth().height(24.dp))
                                        }
                                    }
                                }
                                else {

                                    InfoScreen(
                                        description = "No tokens available.",
                                        primaryColor = primaryColor
                                    )

                                }

                                // Top gradient fade
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .align(Alignment.TopCenter)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(dgenBlack, Color.Transparent)
                                            )
                                        ).zIndex(3f)
                                )

                                // Bottom gradient fade
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, dgenBlack)
                                            )
                                        )
                                        .zIndex(3f)
                                )
                            }
                        }
                    }
                    else -> { /* Do nothing */ }
                }


            }
        }
    }
    
    // Chain selector overlay
    ChainSelectorOverlay(
        isVisible = isChainSelectorVisible,
        selectedChainId = selectedChainId,
        onChainSelected = { chainId ->
            chainId?.let { onChainSelected(it) }
        },
        onDismiss = { isChainSelectorVisible = false },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor
    )
}



fun formatAmount(value: Double): String {
    return if (value == 0.0) "0.0" else String.format("%.6f", value).trimEnd('0').trimEnd('.')
}

fun formatUsd(value: Double): String {
    return if (value == 0.0) "0.00" else String.format("%,.2f", value)
}




