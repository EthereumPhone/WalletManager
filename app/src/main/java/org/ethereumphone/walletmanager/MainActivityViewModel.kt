package org.ethereumphone.walletmanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.UserDataRepository
import com.core.model.UserData
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalLEDController
import com.core.terminalsdk.TerminalSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    val terminalSDK: TerminalSDK?,
    ): ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userData.map {
        MainActivityUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    /** Keeps track of the last route so we can restore LEDs/terminal after
     *  the screen turns off and on (Activity pause/resume cycle).
     */
    private var lastKnownRoute: String? = null
    
    /** Job for debouncing LED updates */
    private var updateMatrixJob: Job? = null
    
    /** Flag to block navigation updates during resume */
    private var isResumingLeds = false
    
    /** Minimum delay between LED updates to prevent flickering */
    private val LED_UPDATE_DEBOUNCE_MS = 300L
    private val LED_UPDATE_RESUME_MS = 1000L

    fun onAppResumed() {
        viewModelScope.launch {
            isResumingLeds = true
            updateMatrixJob?.cancel()
            
            // We have to wait for the systemUI to clear it's animation, or it will cause artifacting
            // once the clear logic is refined, this entire job logic can be removed.
            delay(LED_UPDATE_RESUME_MS)
            updateMatrixInternal(lastKnownRoute)
            isResumingLeds = false
        }
    }

    fun refreshWelcomeMessage() {
        if (terminalSDK == null) return
    }

    fun updateTerminal(currentRoute: String?) {
        if (currentRoute == null) return


    }


    fun updateMatrix(currentRoute: String?) {
        if (currentRoute == null) return
        
        lastKnownRoute = currentRoute
        
        if (isResumingLeds) {
            Log.d("LEDREFRESH", "FROM WMAPP (NAVIGATION) - BLOCKED during resume, saved route: $currentRoute")
            return
        }

        updateMatrixJob?.cancel()
        updateMatrixJob = viewModelScope.launch {

            // Double-check we're still not resuming (in case resume started during delay)
            if (!isResumingLeds) {
                println("LEDREFRESH FROM WMAPP (NAVIGATION)")
                updateMatrixInternal(currentRoute)
            }
        }
    }
    
    private fun updateMatrixInternal(currentRoute: String?) {
        if (currentRoute == null) return

        when (currentRoute) {
            com.feature.home.navigation.homeRoute ->
                TerminalLEDController.displayChadPattern()

            com.feature.receive.navigation.receiveRoute ->
                TerminalLEDController.displayReceivePattern()

            com.example.transactions.navigation.transactionRoute ->
                TerminalLEDController.displayInfoPattern()

            com.feature.paymaster.navigation.paymasterRoute ->
                TerminalLEDController.displayPlusPattern()

            else -> {
                // Default / fallback behaviour
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        //TODO: add code to kill any lingering matrix/terminal content
        viewModelScope.launch {
            terminalSDK?.let {
                it.destroyTouchHandler()
                it.resume(it.ID_STATUSBAR)
            }
        }
    }
}


sealed interface MainActivityUiState {
    object Loading : MainActivityUiState
    data class Success(val userData: UserData): MainActivityUiState
}


private val bootMessage = "WELCOME ONBOARD ヽ(•◡•)ノ"

private val welcomeMessages = listOf(
    "WELCOME BACK  ◕◡◕",
    "Hey Stranger  ⌐■◡■",
    "Look Who's Back  ▀̿◡ ̿▀̿ ̿",
    "Engaging warp drive  ◉◡◉",
    "Big Brain: Activated  ಠ◡ಠ",
    "Refueled n ready ⊹⋆☾⋆⊹✧"
)