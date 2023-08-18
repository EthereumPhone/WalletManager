package org.ethereumphone.walletmanager.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ethereumphone.walletmanager.core.model.Network

class SelectedNetworkViewModel: ViewModel() {
    private val _selectedNetwork = MutableLiveData<Network>()
    val selectedNetwork: LiveData<Network> = _selectedNetwork

    fun setSelectedNetwork(network: Network) {
        _selectedNetwork.value = network
    }
}