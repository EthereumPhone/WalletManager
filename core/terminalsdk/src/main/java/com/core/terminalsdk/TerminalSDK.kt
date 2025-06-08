package com.core.terminalsdk

import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent

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

    /* ----------------------------------------------------------------------------------------- */
    /*  Public façade                                                                            */
    /* ----------------------------------------------------------------------------------------- */

    /** Is the proxy (and therefore the back-screen HAL) available? */
    fun isAvailable(): Boolean = proxy != null

    fun isScreenOn(): Boolean =
        call { mIsOn.invoke(it) as Boolean } ?: false

    fun refresh(bitmap: Bitmap, id: Int): Boolean =
        call { mRefresh.invoke(it, bitmap, id) as Boolean } ?: false

    fun resume(id: Int) = call { mResume.invoke(it, id) }

    /* ----------------------------------------------------------------------------------------- */
    /*  Helpers                                                                                  */
    /* ----------------------------------------------------------------------------------------- */

    private inline fun <T> call(block: (Any) -> T): T? =
        proxy?.let(block)

    fun displayQRCode(onQrCode: () -> Unit, sendTx: () -> Unit) {
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

    fun removeQRCode() {
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Displays the copy bitmap in ReceiveScreen
     */
    fun displayCopyAddress(onCopy: () -> Unit) {
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
                    resume(ID_STATUSBAR)
                    destroyTouchHandler()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    fun removeCopyAddress() {
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }


    /**
     * Displays the copy bitmap in ReceiveScreen
     */
    fun displayTopUp(onTopUp: () -> Unit) {
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
                    resume(ID_STATUSBAR)
                    destroyTouchHandler()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    fun removeTopUp() {
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Manually destroy the current touch handler
     */
    fun destroyTouchHandler() {
        miniDisplayTouchHandler?.destroy()
        miniDisplayTouchHandler = null
    }

    fun finishScreen() {
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
    fun displayBlackText(text: String) {
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