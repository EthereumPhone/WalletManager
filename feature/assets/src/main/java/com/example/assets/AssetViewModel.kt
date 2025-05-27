package com.example.assets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.spamTokens
import com.core.datastore.ExclusionListManager
import com.core.domain.GetAllTokensUsecase
import com.core.domain.UpdateTokensUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.UserData
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject




@HiltViewModel
class AssetViewModel @Inject constructor(
    getAllTokensUsecase: GetAllTokensUsecase,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val updateTokensUseCase: UpdateTokensUseCase,
    private val userDataRepository: UserDataRepository,
    private val exclusionListManager: ExclusionListManager
): ViewModel() {



    val exclusionList: Flow<List<String>> = exclusionListManager.exclusionList


    fun removeFromExclusionList(itemId: String) {
        viewModelScope.launch {
            exclusionListManager.removeFromExclusionList(itemId)
        }
    }

    fun addToExclusionList(itemId: String) {
        viewModelScope.launch {
            exclusionListManager.addToExclusionList(itemId)
        }
    }

    val userData: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )



    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()


    fun refreshData() {
        Log.d("refresh Started", "update started")

        viewModelScope.launch {
            _refreshState.value = true

            try {
                val userData = userDataRepository.userData.first()
                updateTokensUseCase(userData.walletAddress)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _refreshState.value = false
        }
    }

}


sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}