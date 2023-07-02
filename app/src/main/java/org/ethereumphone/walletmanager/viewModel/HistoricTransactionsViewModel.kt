package org.ethereumphone.walletmanager.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ethereumphone.walletmanager.BuildConfig
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction



class HistoricTransactionsViewModel: ViewModel() {
    private val _historicTransactions = MutableLiveData<ArrayList<Transaction>>(ArrayList<Transaction>())
    val historicTransactions: LiveData<ArrayList<Transaction>> = _historicTransactions

    fun getHistoricTransactions(
        selectedNetwork: Network,
        address: String
    ) {
        val url = when(selectedNetwork.chainId) {

            else -> ""
        }.replace("<replace>",address)
    }
}
