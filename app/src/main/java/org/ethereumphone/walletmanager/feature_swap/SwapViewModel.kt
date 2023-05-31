package org.ethereumphone.walletmanager.feature_swap

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.core.domain.usecase.GetTokensMetadataWithExchange
import org.ethereumphone.walletmanager.feature_swap.model.SwapState
import org.ethereumphone.walletmanager.core.util.Resource
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(
    private val getTokensMetadataWithExchange: GetTokensMetadataWithExchange
): ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery = _searchQuery

    private val _state = mutableStateOf(SwapState())
    val state = _state

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null

    fun onSearch(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            getTokensMetadataWithExchange()
                .onEach { result ->
                    when(result) {
                        is Resource.Success -> {
                            _state.value = state.value.copy(
                                tokens = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                        is Resource.Error -> {
                            _state.value = state.value.copy(
                                tokens = result.data ?: emptyList(),
                                isLoading = false
                            )
                            _eventFlow.emit(UIEvent.ShowSnackbar(
                                result.message ?: "Unknown error"
                            ))
                        }
                        is Resource.Loading -> {
                            _state.value = state.value.copy(
                                tokens = result.data ?: emptyList(),
                                isLoading = true
                            )
                        }
                    }
                }.launchIn(this)
        }
    }


    sealed class UIEvent {
        data class ShowSnackbar(val message: String): UIEvent()
    }

}