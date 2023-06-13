package org.ethereumphone.walletmanager.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.core.data.remote.OldExchangeApi

class ExchangeViewModel: ViewModel() {
    private val _exchange = MutableLiveData<String>()
    val exchange: LiveData<String> = _exchange

     fun getExchange(symbol: String?) {
        viewModelScope.launch {
            try {
                val listResult = OldExchangeApi.retrofitService.getExchange(symbol)
                _exchange.value = listResult.price
            } catch (e: Exception) {
                _exchange.value =  "Error: ${e.message}"
            }

        }
    }
}