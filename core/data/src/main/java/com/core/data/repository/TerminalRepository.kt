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
import javax.inject.Singleton

@Singleton
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
                        Log.d("TerminalRepository", "LogTapped event triggered at coordinates: $x, $y")
                        coroutineScope.launch {
                            _events.emit(TerminalEvent.LogTapped)
                            Log.d("TerminalRepository", "LogTapped event emitted successfully")
                        }
                    } catch (e: Exception) {
                        Log.e("TerminalRepository", "Error emitting LogTapped event", e)
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

    suspend fun generateTopUp() {
        sdk?.apply {
            destroyTouchHandler()

            val layoutRenderer = LayoutRenderer(context)
            val qrCodeBitmap = layoutRenderer.renderTopUp()

            refresh(qrCodeBitmap, ID_PERSISTENT)

            miniDisplayTouchHandler = MiniDisplayTouchHandler(
                context,
                MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                    if (action != MotionEvent.ACTION_DOWN) {
                        return@OnTouchListener
                    }
                    try {
                        coroutineScope.launch {
                            _events.emit(TerminalEvent.TopUpTapped)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }
    }

    suspend fun generateSend() {
        sdk?.apply {
            println("ETHOSDEBUGTERMINAL displayQRCode")
            // Clean up any existing touch handler first
            destroyTouchHandler()

            val layoutRenderer = LayoutRenderer(context)
            val qrCodeBitmap = layoutRenderer.renderQrOrSend()

            refresh(qrCodeBitmap, ID_PERSISTENT)

            miniDisplayTouchHandler = MiniDisplayTouchHandler(
                context,
                MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                    if (action != MotionEvent.ACTION_DOWN) {
                        return@OnTouchListener
                    }
                    try {
                        coroutineScope.launch {
                            if (x < 214) {
                                // QR Code area
                                _events.emit(TerminalEvent.QrTapped)
                            } else {
                                _events.emit(TerminalEvent.SendTapped)
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }

    }

    suspend fun generateSwap() {
        sdk?.apply {
            println("ETHOSDEBUGTERMINAL displaySwap")
            destroyTouchHandler()

            val layoutRenderer = LayoutRenderer(context)
            val swapBitmap = layoutRenderer.renderSwap()
            refresh(swapBitmap, ID_PERSISTENT)

            miniDisplayTouchHandler = MiniDisplayTouchHandler(
                context,
                { _, _, action ->
                    if (action == MotionEvent.ACTION_DOWN) {
                        coroutineScope.launch {
                            _events.emit(TerminalEvent.SwapTapped)
                        }
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
    object TopUpTapped: TerminalEvent
    object LogTapped: TerminalEvent
    object SwapTapped: TerminalEvent
    data class LogDetailTapped(val txHash: String = ""): TerminalEvent
}