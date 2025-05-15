package com.example.transactions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.domain.GetTransfersUseCase
import com.core.domain.UpdateTokensUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TransferItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TransactionViewModel @Inject constructor(
    getTransfersUseCase: GetTransfersUseCase,
    private val userDataRepository: UserDataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val transferRepository: TransferRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,

    ): ViewModel() {

    val transferState: StateFlow<TransfersUiState> =
        getTransfersUseCase()
            .map { items ->
                Log.d("TransactionViewModel", "Transfers received from UseCase: ${items.size} items")
                items.forEachIndexed { index, item ->
                    Log.d("TransactionViewModel", "Item[$index]: chainId=${item.chainId}, asset=${item.asset}, value=${item.value}, hash=${item.txHash}, to=${item.to}, from=${item.from}")
                }
                TransfersUiState.Success(items)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransfersUiState.Loading
            )


    val tokenMetadata = tokenMetadataRepository.getTokensMetadata()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val tokenAssetState: StateFlow<TokenAssetUiState> =
        networkBalanceRepository.getNetworksBalance()
            .map { balances ->
                val netWorkAssets = balances.map {
                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                    TokenAsset(
                        address = it.contractAddress,
                        chainId = it.chainId,
                        symbol = name.lowercase(),
                        name = name.lowercase(),
                        balance = it.tokenBalance.toDouble(),
                        decimals = 18
                    )
                }
                .sortedByDescending { it.balance }
                TokenAssetUiState.Success(netWorkAssets)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TokenAssetUiState.Loading
            )


    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()

    fun refreshData() {
        Log.d("refresh Started", "update started")

        viewModelScope.launch {
            _refreshState.value = true

            try {
                val userData = userDataRepository.userData.first()
                transferRepository.refreshTransfers(userData.walletAddress)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _refreshState.value = false
        }
    }

}


sealed interface TokenAssetUiState {
    object Loading: TokenAssetUiState
    object Empty: TokenAssetUiState
    data class Success(
        val assets: List<TokenAsset>
    ): TokenAssetUiState
}


sealed interface TransfersUiState {
    object Loading : TransfersUiState
    data class Success(val transfers: List<TransferItem>) : TransfersUiState

}


