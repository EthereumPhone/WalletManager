package com.feature.swap.exchange

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.exchange.ExchangeRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetSwapTokens
import com.core.model.TokenAsset
import com.core.model.exchange.ExchangeQuote
import com.core.model.exchange.ExchangeResult
import com.core.model.exchange.ExchangeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val exchangeRepository: ExchangeRepository,
    private val userDataRepository: UserDataRepository,
    private val getSwapTokens: GetSwapTokens,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fromAsset = MutableStateFlow<TokenAsset?>(null)
    private val toAsset = MutableStateFlow<TokenAsset?>(null)
    private val fromAmount = MutableStateFlow("")
    private val toAmount = MutableStateFlow("")
    private val isLoading = MutableStateFlow(false)
    private val isSwapping = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val selectedChain = MutableStateFlow(1)
    private val availableAssets: StateFlow<List<TokenAsset>> = combine(
        selectedChain,
        savedStateHandle.getStateFlow("swap_query", "")
    ) { chain, query -> chain to query }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1 to "")
        .let { chainQuery ->
            chainQuery
            // Bridge via separate state flow
            MutableStateFlow(emptyList<TokenAsset>())
        }

    private val _uiState = MutableStateFlow(ExchangeUiState())
    val uiState: StateFlow<ExchangeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var quoteJob: Job? = null
    private var lastQuote: ExchangeQuote? = null

    init {
        viewModelScope.launch {
            // Initialize chain and available assets
            val chain = userDataRepository.userData.first().walletNetwork.toInt()
            selectedChain.value = chain
            refreshAvailableAssets()
            rebuildUi()
        }
    }

    private suspend fun refreshAvailableAssets() {
        val chainId = selectedChain.value
        // Pull once
        getSwapTokens("", chainId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .value
            .let { tokens ->
                _uiState.update { it.copy(availableAssets = tokens) }
            }
    }

    private fun rebuildUi() {
        _uiState.update { current ->
            current.copy(
                isLoading = isLoading.value,
                isSwapping = isSwapping.value,
                error = error.value,
                fromAsset = fromAsset.value,
                toAsset = toAsset.value,
                availableAssets = current.availableAssets,
                fromAmount = fromAmount.value,
                toAmount = toAmount.value,
                selectedChain = selectedChain.value,
                showFromPicker = current.showFromPicker,
                showToPicker = current.showToPicker,
                exchangeRate = computeExchangeRateLabel(),
                needsApproval = lastQuote?.requiresApproval ?: false
            )
        }
    }

    private fun computeExchangeRateLabel(): String {
        val fa = fromAsset.value ?: return ""
        val ta = toAsset.value ?: return ""
        val fromAmt = fromAmount.value.toBigDecimalOrNull() ?: return ""
        val quote = lastQuote ?: return ""
        if (fromAmt.compareTo(BigDecimal.ZERO) <= 0) return ""
        val out = quote.expectedOut
        return if (fromAmt.signum() == 0) "" else out.divide(fromAmt, 6, java.math.RoundingMode.HALF_UP).toPlainString()
    }

    fun setFromAsset(asset: TokenAsset) {
        fromAsset.value = asset
        requestQuoteDebounced()
        rebuildUi()
    }

    fun setToAsset(asset: TokenAsset) {
        toAsset.value = asset
        requestQuoteDebounced()
        rebuildUi()
    }

    fun setFromAmount(amount: String) {
        fromAmount.value = amount.replace(',', '.')
        requestQuoteDebounced()
        rebuildUi()
    }

    fun flipAssets() {
        val f = fromAsset.value
        val t = toAsset.value
        fromAsset.value = t
        toAsset.value = f
        val fa = fromAmount.value
        fromAmount.value = toAmount.value
        toAmount.value = fa
        lastQuote = null
        rebuildUi()
        requestQuoteDebounced()
    }

    fun selectChain(chainId: Int) {
        selectedChain.value = chainId
        lastQuote = null
        viewModelScope.launch { refreshAvailableAssets() }
        rebuildUi()
        requestQuoteDebounced()
    }

    fun showFromPicker(show: Boolean) {
        _uiState.update { it.copy(showFromPicker = show) }
    }

    fun showToPicker(show: Boolean) {
        _uiState.update { it.copy(showToPicker = show) }
    }

    fun requestQuote() {
        requestQuoteInternal()
    }

    private fun requestQuoteDebounced() {
        quoteJob?.cancel()
        quoteJob = viewModelScope.launch {
            delay(350)
            requestQuoteInternal()
        }
    }

    private fun requestQuoteInternal() {
        val f = fromAsset.value ?: return
        val t = toAsset.value ?: return
        val amountStr = fromAmount.value
        val amount = amountStr.toBigDecimalOrNull() ?: return
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return
        isLoading.value = true
        rebuildUi()
        viewModelScope.launch {
            try {
                val quote = exchangeRepository.getQuote(
                    tokenInAddr = f.address,
                    tokenOutAddr = t.address,
                    amountIn = amount.movePointRight(f.decimals),
                    decimalsIn = f.decimals,
                    chainId = selectedChain.value,
                    slippageBps = 50
                )
                lastQuote = quote
                val outAmount = quote.expectedOut
                toAmount.value = outAmount.toPlainString()
                error.value = null
            } catch (e: Throwable) {
                error.value = e.message
            } finally {
                isLoading.value = false
                rebuildUi()
            }
        }
    }

    fun approveIfNeeded() {
        // Simplified flow; approval handled within swap repository when necessary
    }

    fun performSwap() {
        val quote = lastQuote ?: return
        isSwapping.value = true
        rebuildUi()
        viewModelScope.launch {
            when (val res = exchangeRepository.swap(quote)) {
                is ExchangeResult.Success -> _events.tryEmit(res.txHash)
                is ExchangeResult.UserDeclined -> _events.tryEmit("DECLINED")
                is ExchangeResult.Error -> _events.tryEmit(res.message)
            }
            isSwapping.value = false
            rebuildUi()
        }
    }

    fun loadBalances() {
        // Using balances provided in TokenAsset for now
    }

    fun loadPrices() {
        // Prices can be integrated via existing price APIs. Left as future enhancement.
    }
}


