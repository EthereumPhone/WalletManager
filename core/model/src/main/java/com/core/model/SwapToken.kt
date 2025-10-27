package com.core.model


/**
 * Represents a token in the swap interface with its metadata and balance information
 */
data class SwapToken(
    val token: TokenAsset,
    val balance: String = "0.0",
    val fiatBalance: String = "0.00",
    val formattedMaxAmount: String = "0.0",
    val formattedMaxFiatAmount: String = "0.00"
)

/**
 * Test UI State
 */
data class SwapUIState(
    // From token state
    val fromToken: SwapToken? = null,
    val fromCurrentAmount: String = "",
    val fromCurrentFiatAmount: String = "",
    val fromUseMaxAmount: Boolean = false,
    val fromTitle: String = "FROM",
    val fromReadOnly: Boolean = false,
    
    // To token state  
    val toToken: SwapToken? = null,
    val toCurrentAmount: String = "",
    val toCurrentFiatAmount: String = "",
    val toUseMaxAmount: Boolean = false,
    val toTitle: String = "TO",
    val toReadOnly: Boolean = true,
    
    // Callbacks
    val fromOnAmountChange: (String, Boolean) -> Unit = { _, _ -> },
    val fromOnMaxClick: () -> Unit = {},
    val fromOnTokenClick: () -> Unit = {},
    val toOnAmountChange: (String, Boolean) -> Unit = { _, _ -> },
    val toOnMaxClick: () -> Unit = {},
    val toOnTokenClick: () -> Unit = {}
)
