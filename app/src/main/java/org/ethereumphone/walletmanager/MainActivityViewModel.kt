package org.ethereumphone.walletmanager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.UserDataRepository
import com.core.model.UserData
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalLEDController
import com.core.terminalsdk.TerminalSDK
import com.example.transactions.navigation.transactionRoute
import com.feature.home.navigation.homeRoute
import com.feature.paymaster.navigation.paymasterRoute
import com.feature.receive.navigation.receiveRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.nextUp
import kotlin.random.Random


private val LED_UPDATE_DEBOUNCE_MS = 300L
private val LED_UPDATE_RESUME_MS = 1000L

private const val LAST_NAV_ROUTE = "last_known_route"
private const val LAST_MESSAGE_INDEX = "last_message_route"


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val userDataRepository: UserDataRepository,
    val terminalSDK: TerminalSDK?,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userData.map {
        MainActivityUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val userData = userDataRepository.userData

    private val lastKnownRoute = savedStateHandle.getStateFlow(LAST_NAV_ROUTE, "")
    private val lastMessageIndex = savedStateHandle.getStateFlow(LAST_MESSAGE_INDEX, -1)

    /** Job for debouncing LED updates */
    private var updateMatrixJob: Job? = null
    
    /** Flag to block navigation updates during resume */
    private var isResumingLeds = false

    fun onAppResumed() {
        viewModelScope.launch {
            isResumingLeds = true
            updateMatrixJob?.cancel()
            
            // We have to wait for the systemUI to clear it's animation, or it will cause artifacting
            // once the clear logic is refined, this entire job logic can be removed.
            delay(LED_UPDATE_RESUME_MS)
            updateMatrixInternal(lastKnownRoute.value)

            if (lastKnownRoute.value in listOf("", homeRoute)) {
                refreshWelcomeMessage()
            }

            isResumingLeds = false
        }

        // only do this in home scren
    }

    fun refreshWelcomeMessage() {
        if (terminalSDK == null) return

        viewModelScope.launch {
            val text = if(userData.first().isFirstBoot) {
                userDataRepository.setIsFirstBoot(false)
                bootMessage
            } else {
                val index = Random.nextDistinctInt(welcomeMessages.size - 1, lastMessageIndex.value)
                welcomeMessages[index].also { savedStateHandle[LAST_MESSAGE_INDEX] = index }
            }

            terminalSDK.displayBlackText(text)

            delay(2500)
            
            // Only finish the screen if we're still on the home screen
            if (lastKnownRoute.value in listOf("", homeRoute)) {
                terminalSDK.finishScreen()
            } else {
                println("Not on homescreen, lets not finish screen")
            }
        }
    }

    fun updateMatrix(currentRoute: String?) {
        if (currentRoute == null) return


        savedStateHandle[LAST_NAV_ROUTE] = currentRoute
        println("FUNKY TEST $lastKnownRoute")
        
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
            homeRoute -> TerminalLEDController.displayChadPattern()
            receiveRoute -> TerminalLEDController.displayReceivePattern()
            transactionRoute -> TerminalLEDController.displayInfoPattern()
            paymasterRoute -> TerminalLEDController.displayPlusPattern()

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
                it.finishScreen()
            }
        }
    }
}


sealed interface MainActivityUiState {
    object Loading : MainActivityUiState
    data class Success(val userData: UserData): MainActivityUiState
}


fun Random.nextDistinctInt(until: Int, prev: Int): Int {
    while (true) {
        val new = this.nextInt(until)
        if (new != prev) return new
    }
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


sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
}
