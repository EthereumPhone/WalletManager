package com.example.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.domain.GetTokenAssetsBySymbolUseCase
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.example.transactions.navigation.TxArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    //private val txByHashUseCase: GetTxByHashUseCaseUseCase,
): ViewModel() {

    private val assetDetailArgs: TxArgs = TxArgs(savedStateHandle)

    val tx = assetDetailArgs.tx

    val currentTxState: StateFlow<TransactionDetailUiState> =
        txByHashUseCase(
            symbol = assetDetailArgs.tx
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionDetailUiState.Loading
            )


}

sealed interface TransactionDetailUiState {
    object Loading: TransactionDetailUiState
    data class Success(val tx: TransferItem): TransactionDetailUiState

}
