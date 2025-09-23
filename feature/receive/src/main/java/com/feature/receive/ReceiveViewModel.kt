package com.feature.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.TerminalRepository
import com.core.data.repository.UserDataRepository
import com.core.model.UserData
import com.core.terminalsdk.TerminalSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.core.terminalsdk.ReflectiveLedPattern
import kotlinx.coroutines.delay
import com.core.ui.showDgenToast
import com.core.data.repository.TerminalEvent


@HiltViewModel
class ReceiveViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val reflectiveLedPattern: ReflectiveLedPattern?,
    private val terminalRepository: TerminalRepository,
    @ApplicationContext private val appContext: Context,
): ViewModel() {

    val userData = userDataRepository.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserData("","",false, "USD")
        )

    init {
        // Observe terminal events
        viewModelScope.launch {
            onCopyOpened()

            terminalRepository.events.collect { event ->

                if (event == TerminalEvent.CopyTapped) {
                    copyToClipboard(userData.value.walletAddress)
                    showDgenToast(
                        context = appContext,
                        message = "Address copied!"
                    )
                }
            }
        }
    }



    /**
     * Calls this function when the receive screen is opened
     * When opened it displays the copy button
     */
    suspend fun onCopyOpened() {
        try {
            // Use TerminalRepository to generate receive screen
            terminalRepository.generateReceive()
        } catch (e: Exception) {
            Log.e("ReceiveViewModel", "Error displaying receive screen", e)
        }
    }

    fun onScreenOpenedAfterResume() {
        viewModelScope.launch {
            try {
                // Redraw the terminal content after resume
                delay(300)
                terminalRepository.generateReceive()
            } catch (e: Exception) {
                Log.e("ReceiveViewModel", "Error redrawing receive screen after resume", e)
            }
        }
    }

    fun onCopyClosed() {
        viewModelScope.launch {
            try {
                // Use TerminalRepository to dismiss content
                terminalRepository.dismissContent()
            } catch (e: Exception) {
                Log.e("ReceiveViewModel", "Error removing copy terminal screen", e)
            }
        }
    }

    /**
     * Copies the text quietly in the System-Cache
     */
    private fun copyToClipboard(text: String) {
        try {
            reflectiveLedPattern?.displayInfo()
            val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("wallet_address", text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                clipboard.setPrimaryClip(clip)
            } else {
                clipboard.setPrimaryClip(clip)
            }
            viewModelScope.launch {
                delay(2000)
                reflectiveLedPattern?.displayArrowDown()
            }
        } catch (e: Exception) {
            Log.e("ReceiveViewModel", "Error while copying", e)
        }
    }

}

