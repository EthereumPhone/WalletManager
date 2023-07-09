package com.core.model

enum class NetworkChain(
    val chainId: Int,
    val chainName: String, // used for alchemy APIs
    val rpc: String) {
    MAIN(
        chainId = 1,
        chainName = "eth-mainnet",
        rpc = "https://rpc.ankr.com/eth",
    ),
    GOERLI(
        chainId = 5,
        chainName = "eth-goerli",
        rpc = "https://rpc.ankr.com/eth_goerli"
    ),
    OPTIMISM(
        chainId = 10,
        chainName = "opt-mainnet",
        rpc = "https://rpc.ankr.com/optimism"
    ),
    POLYGON(
        chainId = 137,
        chainName = "polygon-mainnet",
        rpc = "https://rpc.ankr.com/polygon"
    ),
    ARBITRUM(
        chainId = 42161,
        chainName = "arb-mainnet",
        rpc = "https://rpc.ankr.com/arbitrum",
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

