package com.example.transactions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetTransfersUseCase
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.model.UserData
import com.core.terminalsdk.TerminalSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


@HiltViewModel
class TransactionViewModel @Inject constructor(
    getTransfersUseCase: GetTransfersUseCase,
    private val userDataRepository: UserDataRepository,
    private val transferRepository: TransferRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val terminalSDK: TerminalSDK?,
    @ApplicationContext private val appContext: Context,
    ): ViewModel() {

    private val onLogOpenedMutex = Mutex()

    val userData = userDataRepository.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserData("","",false, "USD")
        )

    val transferState: StateFlow<TransfersUiState> = getTransfersUseCase()
        .map(TransfersUiState::Success)
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

    fun onScreenOpenedAfterResume() {
        viewModelScope.launch(Dispatchers.Main) {
            onLogOpened()
        }
    }

    suspend fun onLogOpened(){
        onLogOpenedMutex.withLock {
            try {
                //check if terminal sdk is available
                if (terminalSDK?.isAvailable() == true) {
                    // Wait for screen to be ready before drawing
                    while(terminalSDK.isScreenOn() != true) {
                        Log.d("TransactionViewModel", "ETHOSDEBUG: Waiting for secondary screen to be on...")
                        delay(100)
                    }
                    terminalSDK.displayLog {
                        val walletAddress = userData.value.walletAddress
                        if (walletAddress.isNotBlank()) {
                            val url = "https://blockscan.com/address/$walletAddress"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                appContext.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("TransactionViewModel", "Could not open Blockscan for address $walletAddress", e)
                                Toast.makeText(appContext, "Failed to open browser.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.w("TransactionViewModel", "Wallet address is empty, can't open Blockscan.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error on displayLog", e)
            }
        }
    }

    fun onLogClosed() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.removeLog()
                } else {
                    Log.w("TransactionViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error removing log terminal screen", e)
            }
        }
    }

    fun onDetailLogOpened(txHash: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try{
                if (terminalSDK?.isAvailable() == true) {
                    // The user wants the button to say "VIEW TX". I don't have the TerminalSDK API.
                    // I will assume for now that displayLog can be used.
                    // If there's a specific function like `displayButton("VIEW TX")`, it should be used here.
                    terminalSDK.displayDetailLog {
                        val url = "https://blockscan.com/tx/$txHash"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            appContext.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("TransactionViewModel", "Could not open Blockscan for tx $txHash", e)
                            Toast.makeText(appContext, "Failed to open browser.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error on displayLog", e)
            }
        }

    }

    fun onDetailLogClosed() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (terminalSDK?.isAvailable() == true) {
                    onLogOpened()
                } else {
                    Log.w("TransactionViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error removing log terminal screen", e)
            }
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


