package org.ethereumphone.walletmanager.feature_home.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import org.ethereumphone.walletmanager.core.domain.model.NetworkTransfer
import org.ethereumphone.walletmanager.core.domain.repository.TokenExchangeRepository
import org.ethereumphone.walletmanager.core.domain.usecase.GetTokenMetadataWithExchange
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val getTokenMetadataWithExchange: GetTokenMetadataWithExchange

) : ViewModel() {

    //val networkBalanceUiState: StateFlow<HomeScreenState>




}

data class HomeScreenState{
    val isLoading: Boolean = false
    val
}

