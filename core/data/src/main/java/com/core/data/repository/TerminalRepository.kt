package com.core.data.repository

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.viewModelScope
import com.core.terminalsdk.LayoutRenderer
import com.core.terminalsdk.MiniDisplayTouchHandler
import com.core.terminalsdk.TerminalSDK
import com.core.terminalsdk.TerminalSDKWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TerminalRepository @Inject constructor(
    terminalSDKWrapper: TerminalSDKWrapper,
    @ApplicationContext private val context: Context
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    private val _events = MutableSharedFlow<TerminalEvent>()
    val events: SharedFlow<TerminalEvent> = _events

    private val sdk: TerminalSDK? = when (terminalSDKWrapper) {
        is TerminalSDKWrapper.Available -> terminalSDKWrapper.sdk
        TerminalSDKWrapper.Unavailable -> null
    }

    suspend fun dismissContent() {
        sdk?.apply {
            destroyTouchHandler()
            finishScreen()
        }
    }

    suspend fun generateReceive() {
        sdk?.apply {
            println("ETHOSDEBUGTERMINAL displayCopyAddress")
            destroyTouchHandler()

            val layoutRenderer = LayoutRenderer(context)
            val qrCodeBitmap = layoutRenderer.renderCopy()
            refresh(qrCodeBitmap, ID_PERSISTENT)

            miniDisplayTouchHandler = MiniDisplayTouchHandler(
                context,
                { _, _, action ->
                    if (action == MotionEvent.ACTION_DOWN) {
                        coroutineScope.launch {
                            _events.emit(TerminalEvent.CopyTapped)
                        }
                    }
                }
            )
        }
    }

    suspend fun generateLog() {
        sdk?.apply {
            destroyTouchHandler()

            val layoutRenderer = LayoutRenderer(context)
            val qrCodeBitmap = layoutRenderer.renderLogTerminal()

            refresh(qrCodeBitmap, ID_PERSISTENT)

            miniDisplayTouchHandler = MiniDisplayTouchHandler(
                context,
                MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                    if (action != MotionEvent.ACTION_DOWN) {
                        return@OnTouchListener
                    }
                    try {
                        coroutineScope.launch {
                            _events.emit(TerminalEvent.LogTapped)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }
    }

    suspend fun generateDetailLog(txHash: String) {
        sdk?.apply {
            destroyTouchHandler()

            val layoutRenderer = LayoutRenderer(context)
            val qrCodeBitmap = layoutRenderer.renderDetailLogTerminal()

            refresh(qrCodeBitmap, ID_PERSISTENT)

            miniDisplayTouchHandler = MiniDisplayTouchHandler(
                context,
                MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                    if (action != MotionEvent.ACTION_DOWN) {
                        return@OnTouchListener
                    }
                    try {
                        coroutineScope.launch {
                            _events.emit(TerminalEvent.LogDetailTapped(txHash))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }

    }
}


sealed interface TerminalEvent {
    object CopyTapped : TerminalEvent
    object QrTapped: TerminalEvent
    object SendTapped: TerminalEvent
    object LogTapped: TerminalEvent
    data class LogDetailTapped(val txHash: String = ""): TerminalEvent
}