package org.ethereumphone.walletmanager.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ethereumphone.walletmanager.BuildConfig
import org.ethereumphone.walletmanager.core.domain.model.Network
import org.ethereumphone.walletmanager.core.model.Transaction

const val MAINNET = "https://api.etherscan.io/api?module=account&action=txlist&address=<replace>&startblock=0&endblock=99999999&page=1&offset=7&sort=desc&apikey=${BuildConfig.ETHSCAN_API}"
const val GOERLI = "https://api-goerli.etherscan.io/api?module=account&action=txlist&address=<replace>&startblock=0&endblock=99999999&page=1&offset=7&sort=desc&apikey=${BuildConfig.ETHSCAN_API}"
const val OPTIMISM = "https://api-optimistic.etherscan.io/api?module=account&action=txlist&address=$<replace>&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.OPTISCAN_API}"
const val POLYGON = "https://api.polygonscan.com/api?module=account&action=txlist&address=$<replace>&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.POLYSCAN_API}"
const val ARBITRUM = "https://api.arbiscan.io/api?module=account&action=txlistinternal&address=$<replace>&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.ARBISCAN_API}"


class HistoricTransactionsViewModel: ViewModel() {
    private val _historicTransactions = MutableLiveData<ArrayList<Transaction>>(ArrayList<Transaction>())
    val historicTransactions: LiveData<ArrayList<Transaction>> = _historicTransactions

    fun getHistoricTransactions(
        selectedNetwork: Network,
        address: String
    ) {
        val url = when(selectedNetwork.chainId) {
            1 -> MAINNET
            5 -> GOERLI
            10 -> OPTIMISM
            137 -> POLYGON
            42161 -> ARBITRUM
            else -> ""
        }.replace("<replace>",address)
    }
}
