package org.ethereumphone.walletmanager.core.util

import org.ethereumphone.walletmanager.core.database.model.WalletDataEntity

enum class DefaultNetworks(val walletDataEntity: WalletDataEntity) {
    MAIN(
        WalletDataEntity(
            chainId = 1,
            name = "eth-mainnet",
            rpc = "https://rpc.ankr.com/eth",
            address = "",
            networkBalance = 0.0
        )
    ),
    GOERLI(
        WalletDataEntity(
            chainId = 5,
            name = "eth-goerli",
            rpc = "https://rpc.ankr.com/eth_goerli",
            address = "",
            networkBalance = 0.0
        )
    ),
    OPTIMISM(
        WalletDataEntity(
            chainId = 10,
            name = "opt-mainnet",
            rpc = "https://rpc.ankr.com/optimism",
            address = "",
            networkBalance = 0.0
        )
    ),
    GNOSIS(
        WalletDataEntity(
            chainId = 100,
            name = "Gnosis",
            rpc = "https://rpc.ankr.com/gnosis",
            address = "",
            networkBalance = 0.0
        )
    ),
    POLYGON(
        WalletDataEntity(
            chainId = 137,
            name = "polygon-mainnet",
            rpc = "https://rpc.ankr.com/gnosis",
            address = "",
            networkBalance = 0.0
        )
    ),
    OPTIMISM_TEST(
        WalletDataEntity(
            chainId = 420,
            name = "opt-goerli",
            rpc = "https://rpc.ankr.com/optimism_testnet",
            address = "",
            networkBalance = 0.0
        )
    ),
    POLYGON_ZKEVM(
        WalletDataEntity(
            chainId = 1101,
            name = "",
            rpc = "https://rpc.ankr.com/polygon_zkevm",
            address = "",
            networkBalance = 0.0
        )
    ),
    POLYGON_ZKEVM_TESTNET(
        WalletDataEntity(
            chainId = 1142,
            name = "",
            rpc = "https://rpc.ankr.com/polygon_zkevm_testnet",
            address = "",
            networkBalance = 0.0
        )
    ),
    BASE_TESTNET(
        WalletDataEntity(
            chainId = 8453,
            name = "",
            rpc = "https://rpc.ankr.com/base_goerli",
            address = "",
            networkBalance = 0.0
        )
    ),
    ARBITRUM(
        WalletDataEntity(
            chainId = 42161,
            name = "arb-mainnet",
            rpc = "https://rpc.ankr.com/arbitrum",
            address = "",
            networkBalance = 0.0
        )
    ),
    POLYGON_TESTNET(
        WalletDataEntity(
            chainId = 80001,
            name = "polygon-mumbai",
            rpc = "https://rpc.ankr.com/polygon_mumbai",
            address = "",
            networkBalance = 0.0
        )
    ),
    ARBITRUM_NOVA(
        WalletDataEntity(
            chainId = 421611,
            name = "",
            rpc = "https://rpc.ankr.com/arbitrumnova",
            address = "",
            networkBalance = 0.0
        )
    ),
    SCROLL_TESTNET(
        WalletDataEntity(
            chainId = 534351,
            name = "",
            rpc = "https://rpc.ankr.com/scroll_testnet",
            address = "",
            networkBalance = 0.0
        )
    );
    companion object {
        fun getAllNetworkInformationEntities(): List<WalletDataEntity> {
            return values().map { it.walletDataEntity }
        }
    }
}