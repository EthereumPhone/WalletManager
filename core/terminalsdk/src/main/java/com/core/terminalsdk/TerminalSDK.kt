package com.core.terminalsdk

import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TerminalSDK(private val context: Context) {

    /* ----------------------------------------------------------------------------------------- */
    /*  Reflection plumbing                                                                      */
    /* ----------------------------------------------------------------------------------------- */

    private val cls = Class.forName(PROXY_CLS)

    private val mGetInstance = cls.getDeclaredMethod("getInstance")
    private val mScreenOn    = cls.getDeclaredMethod("screenOn")
    private val mScreenOff   = cls.getDeclaredMethod("screenOff")
    private val mIsOn        = cls.getDeclaredMethod("isScreenOn")
    private val mRefresh     = cls.getDeclaredMethod(
        "refresh",               // Kotlin wrapper name
        Bitmap::class.java,      // arg0: Bitmap
        Int::class.javaPrimitiveType  // arg1: int id
    )
    private val mResume      = cls.getDeclaredMethod("resume", Int::class.javaPrimitiveType)

    /* constants fetched reflectively so we don't hard-code */
    val ID_STATUSBAR     = cls.getField("ID_STATUSBAR").getInt(null)
    val ID_INCOMINGCALL  = cls.getField("ID_INCOMINGCALL").getInt(null)
    val ID_NOTIFICATIONS = cls.getField("ID_NOTIFICATIONS").getInt(null)
    val ID_CLOCK         = cls.getField("ID_CLOCK").getInt(null)
    val ID_GOOGLEBYE     = cls.getField("ID_GOOGLEBYE").getInt(null)
    val ID_PERSISTENT    = cls.getField("ID_PERSISTENT").getInt(null)

    /* singleton instance inside the proxy, may be null if service missing */
    private val proxy: Any? = mGetInstance.invoke(null)

    /* reference to current touch handler */
    private var miniDisplayTouchHandler: MiniDisplayTouchHandler? = null

    private val methodMutex = Mutex()

    // Indicates whether a bitmap has been pushed since the last resume.
    private var bitmapPushed: Boolean = false

    private val scope = CoroutineScope(Dispatchers.Main)

    private suspend fun <T> synchronizedBuffer(action: suspend () -> T): T {
        methodMutex.lock()
        try {
            val result = action()
            return result
        } finally {
            methodMutex.unlock()
        }
    }

    /* ----------------------------------------------------------------------------------------- */
    /*  Public façade                                                                            */
    /* ----------------------------------------------------------------------------------------- */

    /** Is the proxy (and therefore the back-screen HAL) available? */
    suspend fun isAvailable(): Boolean {
        println("ETHOSDEBUGTERMINAL isAvailable")
        return synchronizedBuffer { proxy != null }
    }

    suspend fun isScreenOn(): Boolean {
        println("ETHOSDEBUGTERMINAL isScreenOn")
        return synchronizedBuffer { call { mIsOn.invoke(it) as Boolean } == true }
    }

    suspend fun refresh(bitmap: Bitmap, id: Int): Boolean {
        println("ETHOSDEBUGTERMINAL refresh id=$id")
        return synchronizedBuffer {
            val success = call { mRefresh.invoke(it, bitmap, id) as Boolean } ?: false
            if (success) {
                bitmapPushed = true
            }
            success
        }
    }

    suspend fun resume(id: Int) {
        println("ETHOSDEBUGTERMINAL resume id=$id")
        synchronizedBuffer {
            if (bitmapPushed) {
                call { mResume.invoke(it, id) }
                bitmapPushed = false
            } else {
                println("ETHOSDEBUGTERMINAL resume skipped - no prior bitmap pushed")
            }
        }
    }

    /* ----------------------------------------------------------------------------------------- */
    /*  Helpers                                                                                  */
    /* ----------------------------------------------------------------------------------------- */

    private suspend inline fun <T> call(crossinline block: (Any) -> T): T? =
        withContext(Dispatchers.Main) {
            proxy?.let { block(it) }
        }

    suspend fun displayQRCode(onQrCode: () -> Unit, sendTx: () -> Unit) {
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
                    if (x < 214) {
                        // QR Code area
                        onQrCode()
                    } else {
                        sendTx()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeQRCode() {
        println("ETHOSDEBUGTERMINAL removeQRCode")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Displays the copy bitmap in ReceiveScreen
     */
    suspend fun displayCopyAddress(onCopy: () -> Unit) {
        println("ETHOSDEBUGTERMINAL displayCopyAddress")
        // Clean up any existing touch handler first
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val qrCodeBitmap = layoutRenderer.renderCopy()

        refresh(qrCodeBitmap, ID_PERSISTENT)

        miniDisplayTouchHandler = MiniDisplayTouchHandler(
            context,
            MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                if (action != MotionEvent.ACTION_DOWN) {
                    return@OnTouchListener
                }
                try {
                    onCopy()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeCopyAddress() {
        println("ETHOSDEBUGTERMINAL removeCopyAddress")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }


    /**
     * Displays the copy bitmap in ReceiveScreen
     */
    suspend fun displayTopUp(onTopUp: () -> Unit) {
        println("ETHOSDEBUGTERMINAL displayTopUp")
        // Clean up any existing touch handler first
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
                    onTopUp()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeTopUp() {
        println("ETHOSDEBUGTERMINAL removeTopUp")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Displays the log bitmap in LogScreen
     */
    suspend fun displayLog(onLog: () -> Unit) {
        // Clean up any existing touch handler first
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
                    onLog()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeLog() {
        println("ETHOSDEBUGTERMINAL removeLog")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Displays the log bitmap in LogScreen
     */
    suspend fun displayDetailLog(onLog: () -> Unit) {
        // Clean up any existing touch handler first
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
                    onLog()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeDetailLog() {
        println("ETHOSDEBUGTERMINAL removeDetailLog")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Manually destroy the current touch handler
     */
    suspend fun destroyTouchHandler() {
        println("ETHOSDEBUGTERMINAL destroyTouchHandler")
        synchronizedBuffer {
            miniDisplayTouchHandler?.destroy()
            miniDisplayTouchHandler = null
        }
    }

    suspend fun finishScreen() {
        println("ETHOSDEBUGTERMINAL finishScreen")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Displays a simple black screen with the supplied [text] rendered in red and centred.
     * Uses the same dimensions as the existing `black_layout.xml` (428 × 142 px).
     *
     * Calling this will first clean up any active touch-handler and then push the
     * rendered bitmap to the mini-display using the persistent layer (ID_PERSISTENT).
     * No touch processing is installed for this view.
     */
    suspend fun displayBlackText(text: String) {
        println("ETHOSDEBUGTERMINAL displayBlackText text='$text'")
        // Remove any existing touch handling to avoid leaking receivers
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val bitmap = layoutRenderer.renderBlackText(text)

        // Push the bitmap to the display – keep it until explicitly cleared
        refresh(bitmap, ID_PERSISTENT)
    }
}

/* name of the real proxy class */
private const val PROXY_CLS = "android.os.FreemeProxy" 