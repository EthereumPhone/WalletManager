package com.feature.paymaster

import android.annotation.SuppressLint
import android.content.Context
// IBinder is not directly used here for getSystemService, but methods on IPaymasterService might return it or take it.
// For now, it's not strictly needed in imports based on the new approach.
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class PaymasterSDK(private val context: Context) {

    private var paymasterProxyInstance: Any? = null // Will hold the PaymasterProxy instance
    private var getBalanceMethod: Method? = null
    private var registerObserverMethod: Method? = null
    // private var unregisterObserverMethod: Method? = null // Commented out as PaymasterProxy doesn't expose it
    private var queryUpdateMethod: Method? = null

    private var observerProxy: Any? = null
    private lateinit var observerInterface: Class<*>

    private var balanceChangedCallback: ((String) -> Unit)? = null

    companion object {
        private const val PAYMASTER_SERVICE_NAME = "paymaster"
        private const val PAYMASTER_PROXY_CLASS_NAME = "android.os.PaymasterProxy"
        private const val IPAYMASTER_OBSERVER_CLASS_NAME = "android.os.IPaymasterObserver"
        // private const val IPAYMASTER_SERVICE_CLASS_NAME = "android.os.IPaymasterService" // Not directly used for methods now
    }

    @SuppressLint("WrongConstant") // For context.getSystemService
    fun initialize(): Boolean {
        try {
            // 1. Setup Observer Proxy
            observerInterface = Class.forName(IPAYMASTER_OBSERVER_CLASS_NAME)
            val invocationHandler = InvocationHandler { _, method, args ->
                if (method.name == "onBalanceChanged" && args != null && args.isNotEmpty() && args[0] is String) {
                    balanceChangedCallback?.invoke(args[0] as String)
                }
                null // Return type for onBalanceChanged is void
            }
            observerProxy = Proxy.newProxyInstance(
                observerInterface.classLoader,
                arrayOf(observerInterface),
                invocationHandler
            )

            // 2. Get PaymasterProxy instance directly from getSystemService
            // The previous ClassCastException (PaymasterProxy cannot be cast to IBinder)
            // suggests that getSystemService("paymaster") IS returning the PaymasterProxy directly.
            paymasterProxyInstance = context.getSystemService(PAYMASTER_SERVICE_NAME)
                ?: throw IllegalStateException("context.getSystemService(\"$PAYMASTER_SERVICE_NAME\") returned null.")
            
            // Verify it's the correct type, just in case
            val paymasterProxyClass = Class.forName(PAYMASTER_PROXY_CLASS_NAME)
            if (!paymasterProxyClass.isInstance(paymasterProxyInstance)) {
                throw IllegalStateException(
                    "getSystemService returned an unexpected type: ${paymasterProxyInstance?.javaClass?.name}" +
                    " instead of $PAYMASTER_PROXY_CLASS_NAME"
                )
            }

            // 3. Get Methods from PaymasterProxy Class directly
            getBalanceMethod = paymasterProxyClass.getMethod("getBalance")
            registerObserverMethod = paymasterProxyClass.getMethod("registerObserver", observerInterface)
            queryUpdateMethod = paymasterProxyClass.getMethod("queryUpdate")
            // unregisterObserver is not available on the provided PaymasterProxy source
            // If it exists on the actual system class, this line would be:
            // unregisterObserverMethod = paymasterProxyClass.getMethod("unregisterObserver", observerInterface)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            // throw PaymasterSDKInitializationException("Failed to initialize PaymasterSDK: ${e.message}", e)
            return false
        }
    }

    fun registerObserver(callback: (String) -> Unit) {
        this.balanceChangedCallback = callback
        try {
            paymasterProxyInstance?.let { proxy ->
                observerProxy?.let { observer ->
                    registerObserverMethod?.invoke(proxy, observer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentBalance(): String? {
        return try {
            paymasterProxyInstance?.let {
                getBalanceMethod?.invoke(it) as? String
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun queryUpdate() {
        try {
            paymasterProxyInstance?.let {
                queryUpdateMethod?.invoke(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cleanup() {
        // Since PaymasterProxy does not expose unregisterObserver, we can't call it directly.
        // If unregistration is crucial and happens via some other mechanism or if the 
        // actual PaymasterProxy on the device *does* have unregisterObserver, this needs revisiting.
        // For now, we just clear our local references.
        // try {
        //     paymasterProxyInstance?.let { proxy ->
        //         observerProxy?.let { observer ->
        //             unregisterObserverMethod?.invoke(proxy, observer)
        //         }
        //     }
        // } catch (e: Exception) {
        //     e.printStackTrace()
        // }
        balanceChangedCallback = null
        paymasterProxyInstance = null 
    }
}

// Optional: Custom exception for the SDK
// class PaymasterSDKInitializationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) 