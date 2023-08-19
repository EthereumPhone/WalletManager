package com.feature.swap

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    getSwapTokens: GetSwapTokens,
    private val swapRepository: SwapRepository,
    private val savedStateHandle: SavedStateHandle,

    ): ViewModel() {

    private val userData = userDataRepository.userData

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, "")

    val swapTokenUiState: StateFlow<SwapTokenUiState> =
        swapTokenUiState(
            getSwapTokens,
            searchQuery
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SwapTokenUiState.Loading
        )

    private val _assetsUiState = MutableStateFlow(AssetsUiState())
    val assetsUiState = _assetsUiState.asStateFlow()

    private val _amountsUiState = MutableStateFlow(AmountsUiState())
    val amountsUiState = _amountsUiState.asStateFlow()

    private val _selectedTextField = MutableStateFlow(TextFieldSelected.FROM)
    val selectedTextField = _selectedTextField.asStateFlow()

    val exchangeRate: StateFlow<Double> =
        assetsUiState.map { currentState ->
            val fromAsset = currentState.fromAsset
            val toAsset = currentState.toAsset

            if (fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                val address = userData.first().walletAddress

                val test = swapRepository.getQuote(
                    fromAsset.tokenAsset.address,
                    toAsset.tokenAsset.address,
                    1.0,
                    address
                )
                Log.d("getQuote", test.toString())
                test


            } else {
                0.0
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    fun setSelectedTextField(selectedTextField: TextFieldSelected) {
        _selectedTextField.value = selectedTextField
    }

    fun selectAsset(tokenAsset: TokenAsset) {
        _assetsUiState.update { currentState ->
            val fromAsset = currentState.fromAsset
            val toAsset = currentState.toAsset

            when(selectedTextField.value) {
                TextFieldSelected.FROM -> {
                    if (toAsset is SelectedTokenUiState.Selected && toAsset.tokenAsset == tokenAsset) {
                        currentState.copy(
                            toAsset = SelectedTokenUiState.Unselected,
                            fromAsset = SelectedTokenUiState.Selected(tokenAsset)
                        )
                    } else {
                        currentState.copy(
                            fromAsset = SelectedTokenUiState.Selected(tokenAsset)
                        )
                    }
                }

                TextFieldSelected.TO -> {
                    if (fromAsset is SelectedTokenUiState.Selected && fromAsset.tokenAsset == tokenAsset) {
                        currentState.copy(
                            fromAsset = SelectedTokenUiState.Unselected,
                            toAsset = SelectedTokenUiState.Selected(tokenAsset)
                        )
                    } else {
                        currentState.copy(
                            toAsset = SelectedTokenUiState.Selected(tokenAsset)
                        )
                    }
                }
            }
        }
    }

    fun updateAmount(
        amount: String,
    ) {
        val updatedAmount = amount.replace(",",".")

        _amountsUiState.update { currentState ->
            when(selectedTextField.value) {
                TextFieldSelected.FROM -> {
                    currentState.copy(
                        fromAmount = updatedAmount,
                        toAmount = BigDecimal(updatedAmount).multiply(BigDecimal(exchangeRate.value)).toPlainString()
                    )
                }
                TextFieldSelected.TO -> {
                    currentState.copy(
                        fromAmount = BigDecimal.ONE.divide(BigDecimal(updatedAmount)).toPlainString(),
                        toAmount = updatedAmount
                    )
                }
            }
        }
    }

    fun switchTokens() {
        _assetsUiState.update { currentState ->
            val currentFromAsset = currentState.fromAsset

            currentState.copy(
                fromAsset = currentState.toAsset,
                toAsset = currentFromAsset
            )
        }

        _amountsUiState.update { currentState ->
            val currentFromAmount = currentState.fromAmount

            currentState.copy(
                fromAmount = currentState.toAmount,
                toAmount = currentFromAmount,
                )

        }
    }

    fun updateSearchQuery(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    fun swap() {
        viewModelScope.launch {
            val (fromAsset, toAsset) = assetsUiState.value
            val fromAmount = amountsUiState.value.fromAmount

            if(fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                swapRepository.swap(
                    fromAsset.tokenAsset.address,
                    toAsset.tokenAsset.address,
                    fromAmount.toDouble()
                )
            }
        }
    }
}

enum class TextFieldSelected {
    FROM, TO
}

private const val SEARCH_QUERY = "searchQuery"


@OptIn(ExperimentalCoroutinesApi::class)
private fun swapTokenUiState(
    getSwapTokens: GetSwapTokens,
    searchQuery: Flow<String>
): Flow<SwapTokenUiState> =
    searchQuery.flatMapLatest { query ->
        getSwapTokens(query)
            .map { result ->
                if(result.isEmpty()) {
                    SwapTokenUiState.Loading
                } else {
                    SwapTokenUiState.Success(result)

                }
            }
    }

sealed interface SwapTokenUiState {
    object Loading: SwapTokenUiState
    object Error: SwapTokenUiState
    data class Success(val tokenAssets: List<TokenAsset>): SwapTokenUiState
}

data class AmountsUiState(
    val fromAmount: String = "",
    val toAmount: String = "",
)

data class AssetsUiState(
    val fromAsset: SelectedTokenUiState = SelectedTokenUiState.Unselected,
    val toAsset: SelectedTokenUiState = SelectedTokenUiState.Unselected
)

sealed interface SelectedTokenUiState {
    object Unselected: SelectedTokenUiState
    data class Selected(
        val tokenAsset: TokenAsset,
    ): SelectedTokenUiState
}

