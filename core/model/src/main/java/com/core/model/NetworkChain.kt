package com.core.model

enum class NetworkChain(
    val chainId: Int,
    val chainName: String,       // Alchemy subdomain, e.g. "base-mainnet"
    val displayName: String,     // human-readable network name
    val nativeSymbol: String,    // gas-token ticker
    val nativeName: String,      // gas-token name
    val nativeDecimals: Int = 18,
) {
    MAINNET(
        chainId = 1,
        chainName = "eth-mainnet",
        displayName = "Ethereum",
        nativeSymbol = "ETH",
        nativeName = "Ethereum",
    ),
    //SEPOLIA(chainId = 11155111, chainName = "eth-sepolia",),
    OPTIMISM(
        chainId = 10,
        chainName = "opt-mainnet",
        displayName = "Optimism",
        nativeSymbol = "ETH",
        nativeName = "Ethereum",
    ),
    POLYGON(
        chainId = 137,
        chainName = "polygon-mainnet",
        displayName = "Polygon",
        // Polygon rebranded MATIC -> POL, but the app fetches/groups native price under
        // "MATIC"; keep that ticker so the existing price flow keeps working.
        nativeSymbol = "MATIC",
        nativeName = "Polygon",
    ),
    ARBITRUM(
        chainId = 42161,
        chainName = "arb-mainnet",
        displayName = "Arbitrum",
        nativeSymbol = "ETH",
        nativeName = "Ethereum",
    ),
    BASE(
        chainId = 8453,
        chainName = "base-mainnet",
        displayName = "Base",
        nativeSymbol = "ETH",
        nativeName = "Ethereum",
    ),
    ZORA(
        chainId = 7777777,
        chainName = "zora-mainnet",
        displayName = "Zora",
        nativeSymbol = "ETH",
        nativeName = "Ethereum",
    ),
    BNB(
        chainId = 56,
        chainName = "bnb-mainnet",
        displayName = "BNB Chain",
        nativeSymbol = "BNB",
        nativeName = "BNB",
    ),
    AVALANCHE(
        chainId = 43114,
        chainName = "avax-mainnet",
        displayName = "Avalanche",
        nativeSymbol = "AVAX",
        nativeName = "Avalanche",
    ),
    MONAD(
        chainId = 143,
        chainName = "monad-mainnet",
        displayName = "Monad",
        nativeSymbol = "MON",
        nativeName = "Monad",
    ),
    APECHAIN(
        chainId = 33139,
        chainName = "apechain-mainnet",
        displayName = "ApeChain",
        nativeSymbol = "APE",
        nativeName = "ApeCoin",
    );

    /** Group id used for this chain's native (gas) token, e.g. "network_eth". */
    val nativeGroupId: String get() = "network_${nativeSymbol.lowercase()}"

    companion object {
        fun getAllNetworkChains(): List<NetworkChain> {
            return NetworkChain.values().toList()
        }

        fun getNetworkByChainId(chainId: Int): NetworkChain? {
            for (network in NetworkChain.values()) {
                if (network.chainId == chainId) return network
            }
            return null
        }
    }
}

