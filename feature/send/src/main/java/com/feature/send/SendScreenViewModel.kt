package com.feature.send

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.SendRepository
import com.core.data.repository.SendRepositoryWalletSDK
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetGroupedTokenAssets
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.domain.GetTransfersUseCase
import com.core.domain.UpdateTokensUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenBalance
import com.core.model.Transfer
import com.core.model.TransferItem
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val sendRepository: SendRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
): ViewModel() {

    val userAddress: StateFlow<String> =
        userDataRepository.userData.map {
            it.walletAddress
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )


    val networkBalanceState: StateFlow<AssetUiState> =
        networkBalanceRepository.getNetworksBalance()
            .map { balances  ->

                val assets = balances.map {

                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                    TokenAsset(it.contractAddress,it.chainId,name,name,it.tokenBalance.toDouble())
                }
                AssetUiState.Success(assets)
            }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetUiState.Loading
        )

    private val _maxAmount = MutableStateFlow("")
    val maxAmount: Flow<String> = _maxAmount .asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: Flow<String> = _amount .asStateFlow()

    private val _toAddress = MutableStateFlow("")
    val toAddress: Flow<String> = _toAddress .asStateFlow()

    private val _selectedAsset = MutableStateFlow<SelectedTokenUiState>(SelectedTokenUiState.Unselected)
    private val selectedAsset: StateFlow<SelectedTokenUiState> = _selectedAsset.asStateFlow()

    fun send(){
        viewModelScope.launch {

//            sendRepository.sendTo(
//                chainId = selectedAsset.value,
//                toAddress = toAddress.toString(),
//                data = "",
//                value = amount.toString()
//            )
        }
    }

    fun changeSelectedAsset(tokenAsset: TokenAsset) {
        when(_selectedAsset.value) {
            is SelectedTokenUiState.Selected -> {
                _selectedAsset.value = SelectedTokenUiState.Selected(tokenAsset)
            }
            is SelectedTokenUiState.Unselected -> {
                _selectedAsset.value = SelectedTokenUiState.Selected(tokenAsset)
            }
        }
    }
}


sealed interface SelectedTokenUiState {
    object Unselected: SelectedTokenUiState
    data class Selected(val tokenAsset: TokenAsset): SelectedTokenUiState
}

sealed interface AssetUiState {
    object Loading: AssetUiState
    object Error: AssetUiState
    object Empty: AssetUiState
    data class Success(
        val assets: List<TokenAsset>
    ): AssetUiState
}



