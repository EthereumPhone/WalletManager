package com.feature.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.core.ui.showCustomToast
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenTurqoise
import androidx.compose.ui.text.font.FontWeight
import com.core.ui.util.PitagonsSans

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val terminalSDK: TerminalSDK?,
    @ApplicationContext private val appContext: Context,
): ViewModel() {

    val userData = userDataRepository.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserData("","",false, "USD")
        )

    /**
     * Calls this function when the receive screen is opened
     *
     * When opened it displays the copy button
     */
    fun onCopyOpened(){
        try{
            //check if terminal sdk is available
            if (terminalSDK?.isAvailable() == true) {
                terminalSDK.displayCopyAddress {
                    copyToClipboard(userData.value.walletAddress)
                    viewModelScope.launch(Dispatchers.Main) {
                        appContext.showCustomToast(
                            message = "Address copied!",
                            fontFamily = PitagonsSans,
                            fontWeight = FontWeight.SemiBold,
                            backgroundColor = dgenOcean,
                            textColor = dgenTurqoise,
                            duration = Toast.LENGTH_SHORT
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ReceiveViewModel", "Error copying address", e)
        }
    }

    fun onCopyClosed() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.removeCopyAddress()
                } else {
                    Log.w("ReceiveViewModel", "TerminalSDK not available")
                }
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
            val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("wallet_address", text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                clipboard.setPrimaryClip(clip)
            } else {
                clipboard.setPrimaryClip(clip)
            }
        } catch (e: Exception) {
            Log.e("ReceiveViewModel", "Error while copying", e)
        }
    }

}

