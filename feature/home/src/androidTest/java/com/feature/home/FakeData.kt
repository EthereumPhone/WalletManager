package com.feature.home

import com.core.model.SwapToken
import com.core.model.SwapUIState
import com.core.model.TokenAsset
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenGroupAssetOverview

/**
 * Centralized test fixtures for wallet assets and related UI models.
 * Use these in UI tests across Home and (by copy) Swap modules to preview different states.
 */
object FakeData {

    // Base chain (8453) - Native ETH (sentinel zero address), 18 decimals
    val ethBase: TokenAsset = TokenAsset(
        address = "0x0000000000000000000000000000000000000000",
        chainId = 8453,
        symbol = "ETH",
        name = "Ethereum",
        balance = 1.2345,
        decimals = 18,
        logoUrl = null,
        swappable = true
    )

    // Base chain (8453) - USDC, 6 decimals
    val usdcBase: TokenAsset = TokenAsset(
        address = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913",
        chainId = 8453,
        symbol = "USDC",
        name = "USD Coin",
        balance = 250.00,
        decimals = 6,
        logoUrl = "https://ethereum-optimism.github.io/data/USDC/logo.png",
        swappable = true
    )

    // Ethereum mainnet (1) - USDC, 6 decimals
    val usdcMainnet: TokenAsset = TokenAsset(
        address = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
        chainId = 1,
        symbol = "USDC",
        name = "USD Coin",
        balance = 10420.10,
        decimals = 6,
        logoUrl = "https://cryptologos.cc/logos/usd-coin-usdc-logo.png",
        swappable = true
    )

    // Ethereum mainnet (1) - DAI, 18 decimals
    val daiMainnet: TokenAsset = TokenAsset(
        address = "0x6B175474E89094C44Da98b954EedeAC495271d0F",
        chainId = 1,
        symbol = "DAI",
        name = "Dai Stablecoin",
        balance = 1534.25,
        decimals = 18,
        logoUrl = "https://cryptologos.cc/logos/multi-collateral-dai-dai-logo.png",
        swappable = true
    )

    // Ethereum mainnet (1) - USDT, 6 decimals
    val usdtMainnet: TokenAsset = TokenAsset(
        address = "0xdAC17F958D2ee523a2206206994597C13D831ec7",
        chainId = 1,
        symbol = "USDT",
        name = "Tether USD",
        balance = 256.75,
        decimals = 6,
        logoUrl = "https://cryptologos.cc/logos/tether-usdt-logo.png",
        swappable = true
    )

    // Ethereum mainnet (1) - LINK, 18 decimals
    val linkMainnet: TokenAsset = TokenAsset(
        address = "0x514910771AF9Ca656af840dff83E8264EcF986CA",
        chainId = 1,
        symbol = "LINK",
        name = "Chainlink",
        balance = 89.34,
        decimals = 18,
        logoUrl = "https://cryptologos.cc/logos/chainlink-link-logo.png",
        swappable = true
    )

    // Polygon (137) - WETH, 18 decimals
    val wethPolygon: TokenAsset = TokenAsset(
        address = "0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619",
        chainId = 137,
        symbol = "WETH",
        name = "Wrapped Ether",
        balance = 0.753,
        decimals = 18,
        logoUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2/logo.png",
        swappable = true
    )

    // Example of a token with zero balance (to test filtering/visibility)
    val zeroBalanceToken: TokenAsset = TokenAsset(
        address = "0x0000000000000000000000000000000000000001",
        chainId = 8453,
        symbol = "TEST0",
        name = "Zero Balance Token",
        balance = 0.0,
        decimals = 18,
        logoUrl = null,
        swappable = false
    )

    val allAssets: List<TokenAsset> = listOf(
        ethBase,
        usdcBase,
        usdcMainnet,
        daiMainnet,
        usdtMainnet,
        linkMainnet,
        wethPolygon,
        zeroBalanceToken
    )

    // Priced variants for UI that shows fiat amounts
    val withPrice: List<TokenAssetWithPrice> = listOf(
        ethBase.toTokenAssetWithPrice(fiatAmount = 3500.00),
        usdcBase.toTokenAssetWithPrice(fiatAmount = 250.00),
        usdcMainnet.toTokenAssetWithPrice(fiatAmount = 10420.10),
        daiMainnet.toTokenAssetWithPrice(fiatAmount = 1534.25),
        usdtMainnet.toTokenAssetWithPrice(fiatAmount = 256.75),
        linkMainnet.toTokenAssetWithPrice(fiatAmount = 3150.00), // 89.34 * ~35.25
        wethPolygon.toTokenAssetWithPrice(fiatAmount = 2100.00),
        zeroBalanceToken.toTokenAssetWithPrice(fiatAmount = 0.0)
    )

    // Grouped overviews as expected by Home's TokenCardCarousel
    val groupedOverviews: List<TokenGroupAssetOverview> = listOf(
        TokenGroupAssetOverview(
            groupId = "8453_0x0000000000000000000000000000000000000000",
            symbol = "ETH",
            name = "Ethereum",
            logoUrl = null,
            totalBalance = 1.2345,
            formattedBalance = "1.2345",
            totalFiatBalance = 3500.00,
            formattedFiatBalance = "3,500.00",
            exchangeCurrency = "usd"
        ),
        TokenGroupAssetOverview(
            groupId = "8453_0x833589fcd6edb6e08f4c7c32d4f71b54bda02913",
            symbol = "USDC",
            name = "USD Coin",
            logoUrl = "https://ethereum-optimism.github.io/data/USDC/logo.png",
            totalBalance = 250.00,
            formattedBalance = "250.00",
            totalFiatBalance = 250.00,
            formattedFiatBalance = "250.00",
            exchangeCurrency = "usd"
        ),
        TokenGroupAssetOverview(
            groupId = "1_0x6b175474e89094c44da98b954eedeac495271d0f",
            symbol = "DAI",
            name = "Dai Stablecoin",
            logoUrl = "https://cryptologos.cc/logos/multi-collateral-dai-dai-logo.png",
            totalBalance = 1534.25,
            formattedBalance = "1,534.25",
            totalFiatBalance = 1534.25,
            formattedFiatBalance = "1,534.25",
            exchangeCurrency = "usd"
        )
    )

    // Convenience: prebuilt Success state for Home screen
    fun groupedSuccess(): GroupedAssetsUiState = GroupedAssetsUiState.Success(groupedOverviews)

    // Convenience: a sample swap UI state (ETH -> USDC)
    val sampleSwapUiState: SwapUIState = SwapUIState(
        fromToken = SwapToken(
            token = ethBase,
            balance = "1.2345",
            fiatBalance = "3,500.00",
            formattedMaxAmount = "1.2345",
            formattedMaxFiatAmount = "3,500.00"
        ),
        fromCurrentAmount = "0.25",
        fromCurrentFiatAmount = "875.00",
        fromUseMaxAmount = false,
        toToken = SwapToken(
            token = usdcBase,
            balance = "250.00",
            fiatBalance = "250.00",
            formattedMaxAmount = "250.00",
            formattedMaxFiatAmount = "250.00"
        ),
        toCurrentAmount = "875.00",
        toCurrentFiatAmount = "875.00",
        toUseMaxAmount = false
    )
}


