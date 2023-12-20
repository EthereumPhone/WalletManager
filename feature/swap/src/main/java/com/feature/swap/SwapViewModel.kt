package com.feature.swap

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.SwapRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetSwapTokens
import com.core.domain.QueryTokenAssetsByNetwork
import com.core.model.TokenAsset
import com.core.model.UserData
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ethereumphone.walletsdk.WalletSDK
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    getSwapTokens: GetSwapTokens,
    queryTokenAssetsByNetwork: QueryTokenAssetsByNetwork,
    private val swapRepository: SwapRepository,
    private val savedStateHandle: SavedStateHandle,
    ): ViewModel() {

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, "")

    val swapTokenUiState: StateFlow<SwapTokenUiState> =
        swapTokenUiState(
            userDataRepository,
            getSwapTokens,
            searchQuery
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SwapTokenUiState.Loading
        )

    private val _swapAssetsUiState = MutableStateFlow(AssetsUiState())
    val swapAssetsUiState = _swapAssetsUiState.asStateFlow()

    private val _amountsUiState = MutableStateFlow(AmountsUiState())
    val amountsUiState = _amountsUiState.asStateFlow()


    private val _selectedTextField = MutableStateFlow(TextFieldSelected.FROM)
    val selectedTextField = _selectedTextField.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    val exchangeRate: StateFlow<Double> =
        swapAssetsUiState.map { currentState ->
            val fromAsset = currentState.fromAsset
            val toAsset = currentState.toAsset

            if (fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                _isSyncing.value = true
                val address = userDataRepository.userData.first().walletAddress

                val quote = swapRepository.getQuote(
                    fromAsset.tokenAsset.address,
                    toAsset.tokenAsset.address,
                    1.0,
                    address,
                    userDataRepository.userData.first().walletNetwork.toInt()
                )
                Log.d("getQuote", quote.toString())
                _isSyncing.value = false
                quote
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
        viewModelScope.launch {
            _swapAssetsUiState.update { currentState ->
                val fromAsset = currentState.fromAsset
                val toAsset = currentState.toAsset

                when(_selectedTextField.value) {
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
    }

    fun updateAmount(
        selectedTextField: TextFieldSelected,
        amount: String,
    ) {
        val updatedAmount = amount.replace(",",".")

        viewModelScope.launch {
            exchangeRate.collect { rate ->
                _amountsUiState.update { currentState ->
                    Log.d("rate", rate.toString())
                    when(selectedTextField) {
                        TextFieldSelected.FROM -> {
                            val amountText = if (updatedAmount == "") "" else (BigDecimal(updatedAmount)
                                .multiply(BigDecimal(rate)))
                                .setScale(4, RoundingMode.HALF_EVEN)
                                .stripTrailingZeros()
                                .toPlainString()

                            currentState.copy(
                                fromAmount = updatedAmount,
                                toAmount = amountText
                            )
                        }
                        TextFieldSelected.TO -> {
                            val amountText = if(rate != 0.0) {
                                if(updatedAmount == "") "" else BigDecimal(updatedAmount)
                                    .divide(BigDecimal(rate), 4, RoundingMode.HALF_EVEN)
                                    .stripTrailingZeros()
                                    .toPlainString()
                            } else {
                                "0"
                            }

                            currentState.copy(
                                fromAmount = amountText,
                                toAmount = updatedAmount
                            )
                        }
                    }
                }
            }
        }
    }

    fun switchTokens() {
        _swapAssetsUiState.update { currentState ->
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

    fun swap(callback: (String) -> Unit) {
        viewModelScope.launch {
            val (fromAsset, toAsset) = swapAssetsUiState.value
            val fromAmount = amountsUiState.value.fromAmount
            try {
                if(fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                    val result = swapRepository.swap(
                        fromAsset.tokenAsset.address,
                        toAsset.tokenAsset.address,
                        fromAmount.toDouble()
                    )
                    callback(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
    userDataRepository: UserDataRepository,
    getSwapTokens: GetSwapTokens,
    searchQuery: Flow<String>
): Flow<SwapTokenUiState> = searchQuery.flatMapLatest { query ->
        getSwapTokens(
            query,
            userDataRepository.userData.first().walletNetwork.toInt()
        ).asResult()
            .mapLatest { result ->
                when(result) {
                    is Result.Error -> { SwapTokenUiState.Error }
                    is Result.Loading -> { SwapTokenUiState.Loading }
                    is Result.Success -> { SwapTokenUiState.Success(result.data) }
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

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}
