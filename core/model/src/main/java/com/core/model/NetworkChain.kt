package com.core.model

enum class NetworkChain(
    val chainId: Int,
    val chainName: String, // used for alchemy APIs)
) {
    MAINNET(
        chainId = 1,
        chainName = "eth-mainnet",
    ),
    SEPOLIA(
        chainId = 11155111,
        chainName = "eth-sepolia",
    ),
    OPTIMISM(
        chainId = 10,
        chainName = "opt-mainnet",
    ),
    POLYGON(
        chainId = 137,
        chainName = "polygon-mainnet",
    ),
    ARBITRUM(
        chainId = 42161,
        chainName = "arb-mainnet",
    ),
    BASE(
        chainId = 8453,
        chainName = "base-mainnet",
    ),
    ZORA(
        chainId = 7777777,
        chainName = "eth-mainnet"
    );

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

