package com.feature.swap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.SwapRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetSwapTokens
import com.core.model.TokenAsset
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    getSwapTokens: GetSwapTokens,
    swapRepository: SwapRepository,
    private val savedStateHandle: SavedStateHandle,

    ): ViewModel() {

    private val userAddress: StateFlow<String> =
        userDataRepository.userData.map {
            it.walletAddress
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val swapTokenUiState: StateFlow<SwapTokenUiState> =
        swapTokenUiState(getSwapTokens
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SwapTokenUiState.Loading
        )

    // this value makes sure that exchange only gets calculated when switchTokens as switched both tokens.
    val tokenSwitched: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val fromAsset = savedStateHandle.getStateFlow<SelectedTokenUiState>(FROM_ASSET, SelectedTokenUiState.Unselected)
    val toAsset = savedStateHandle.getStateFlow<SelectedTokenUiState>(TO_ASSET, SelectedTokenUiState.Unselected)
    val fromAmount = savedStateHandle.getStateFlow(FROM_AMOUNT, 0.0)
    val toAmount = savedStateHandle.getStateFlow(TO_AMOUNT, 0.0)

    val exchangeRate: StateFlow<Double> =
        combine(
            tokenSwitched,
            fromAsset,
            toAsset
        ) { tokenSwitched, fromAsset, toAsset ->
            if (tokenSwitched && fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                swapRepository.getQuote(
                    fromAsset.tokenAsset.address,
                    toAsset.tokenAsset.address,
                    1.0,
                    userAddress.value
                )
            } else {
                0.0
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    fun selectFromAsset(tokenAsset: TokenAsset) {
        val currentToAsset = toAsset.value

        if (currentToAsset is SelectedTokenUiState.Selected
            && currentToAsset.tokenAsset != tokenAsset) {
            savedStateHandle[TO_ASSET] = SelectedTokenUiState.Unselected
        }
        savedStateHandle[FROM_ASSET] = SelectedTokenUiState.Selected(tokenAsset)
    }

    fun selectToAsset(tokenAsset: TokenAsset) {
        val currentToAsset = fromAsset.value

        if (currentToAsset is SelectedTokenUiState.Selected
            && currentToAsset.tokenAsset != tokenAsset) {
            savedStateHandle[FROM_ASSET] = SelectedTokenUiState.Unselected
        }
        savedStateHandle[TO_ASSET] = SelectedTokenUiState.Selected(tokenAsset)
    }

    fun updateFromAmount(amount: String) {
        savedStateHandle[FROM_AMOUNT] = amount.toDouble()
        savedStateHandle[TO_AMOUNT]  = amount.toDouble() * exchangeRate.value

    }

    fun updateToAmount(amount: String) {
        savedStateHandle[FROM_AMOUNT] = 1.0/amount.toDouble()
        savedStateHandle[TO_AMOUNT]  = amount.toDouble()
    }

    fun switchTokens() {
        tokenSwitched.value = false
        val currentFromAsset = fromAsset.value
        val currentToAsset = fromAmount.value

        savedStateHandle[FROM_ASSET] = toAsset.value
        savedStateHandle[FROM_AMOUNT] = toAmount.value

        savedStateHandle[TO_ASSET] = currentFromAsset
        savedStateHandle[TO_AMOUNT] = currentToAsset
        tokenSwitched.value = true
    }


}

private const val FROM_ASSET = "fromAsset"
private const val TO_ASSET = "toAsset"
private const val FROM_AMOUNT = "fromAmount"
private const val TO_AMOUNT = "toAmount"


private fun swapTokenUiState(
    getSwapTokens: GetSwapTokens
): Flow<SwapTokenUiState> =
    getSwapTokens()
        .asResult()
        .map { result ->
            when(result) {
                is Result.Success -> { SwapTokenUiState.Success(result.data) }
                is Result.Loading -> { SwapTokenUiState.Loading }
                is Result.Error -> { SwapTokenUiState.Error }
            }
        }

sealed interface SwapTokenUiState {
    object Loading: SwapTokenUiState
    object Error: SwapTokenUiState
    data class Success(val tokenMetadata: List<TokenAsset>): SwapTokenUiState
}

sealed interface SelectedTokenUiState {
    object Unselected: SelectedTokenUiState
    data class Selected(
        val tokenAsset: TokenAsset,
    ): SelectedTokenUiState
}

