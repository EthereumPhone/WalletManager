package com.core.terminalsdk;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;

import java.lang.reflect.Method;

/**
 * Mini Display Touch Handler using reflection to access the system service.
 * This maintains the same API as before but uses the new service-based approach.
 * 
 * No compile-time dependency on com.freeme.backscreen package.
 */
public class MiniDisplayTouchHandler {
    private static final String TAG = "MiniDisplayTouchHandler";
    private static final String SERVICE_NAME = "minidisplay.touch";
    
    // Interface tokens (must match what the service expects)
    private static final String SERVICE_INTERFACE_TOKEN = "minidisplay.touch";
    private static final String LISTENER_INTERFACE_TOKEN = "com.freeme.backscreen.IMiniDisplayTouchListener";
    
    private final Context mContext;
    private OnTouchListener touchListener;
    private Object mService; // IMiniDisplayTouchService instance
    private ListenerBinder mListenerBinder; // Our binder implementation
    private boolean isConnected = false;
    
    // Track active instance for cleanup
    private static MiniDisplayTouchHandler activeInstance = null;
    
    // Callback interface for touch events (keeping original API)
    public interface OnTouchListener {
        void onTouch(float x, float y, int action);
    }
    
    /**
     * Binder implementation for IMiniDisplayTouchListener
     */
    private class ListenerBinder extends Binder {
        private static final int TRANSACTION_ON_TOUCH = 1;
        
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) 
                throws RemoteException {
            if (code == TRANSACTION_ON_TOUCH) {
                data.enforceInterface(LISTENER_INTERFACE_TOKEN);
                
                // Read parameters
                float x = data.readFloat();
                float y = data.readFloat();
                int action = data.readInt();
                long eventTime = data.readLong();
                
                // Dispatch to our listener on main thread
                mContext.getMainExecutor().execute(() -> {
                    if (touchListener != null && !isDestroyed()) {
                        touchListener.onTouch(x, y, action);
                        
                        if (action == MotionEvent.ACTION_UP) {
                            Log.d(TAG, "Touch at " + x + "," + y);
                        }
                    }
                });
                
                reply.writeNoException();
                return true;
            }
            
            return super.onTransact(code, data, reply, flags);
        }
        
        // Helper to check if handler is destroyed
        private boolean isDestroyed() {
            return !isConnected || touchListener == null;
        }
    }
    
    public MiniDisplayTouchHandler(Context context, OnTouchListener listener) {
        // Clean up any previous instance
        if (activeInstance != null) {
            activeInstance.destroy();
        }
        
        this.mContext = context.getApplicationContext();
        this.touchListener = listener;
        this.mListenerBinder = new ListenerBinder();
        
        // Initialize connection to service
        initializeService();
        
        // Track this as the active instance
        activeInstance = this;
    }
    
    private void initializeService() {
        try {
            // Get ServiceManager and retrieve the service
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
            IBinder serviceBinder = (IBinder) getServiceMethod.invoke(null, SERVICE_NAME);
            
            if (serviceBinder == null) {
                Log.e(TAG, "Mini display touch service not found");
                return;
                //throw new IllegalStateException("Mini display touch service not available");
            }
            
            // Create proxy for the service
            mService = createServiceProxy(serviceBinder);
            
            // Register our listener
            registerListener();
            
            isConnected = true;
            Log.d(TAG, "Touch handler initialized successfully via service");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize service connection", e);
            throw new RuntimeException("Failed to connect to mini display touch service", e);
        }
    }
    
    private Object createServiceProxy(IBinder binder) throws Exception {
        // We'll use Parcel to call methods on the service
        return new ServiceProxy(binder);
    }
    
    /**
     * Proxy class to handle service calls via Parcel transactions
     */
    private class ServiceProxy {
        private static final int TRANSACTION_REGISTER = 1;
        private static final int TRANSACTION_UNREGISTER = 2;
        private static final int TRANSACTION_IS_REGISTERED = 3;
        private static final int TRANSACTION_GET_ACTIVE = 4;
        
        private final IBinder mRemote;
        
        ServiceProxy(IBinder remote) {
            mRemote = remote;
        }
        
        boolean registerTouchListener(IBinder listener, String packageName) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(SERVICE_INTERFACE_TOKEN);
                data.writeStrongBinder(listener);
                data.writeString(packageName);
                
                mRemote.transact(TRANSACTION_REGISTER, data, reply, 0);
                reply.readException();
                return reply.readInt() != 0;
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        
        void unregisterTouchListener(IBinder listener) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(SERVICE_INTERFACE_TOKEN);
                data.writeStrongBinder(listener);
                
                mRemote.transact(TRANSACTION_UNREGISTER, data, reply, 0);
                reply.readException();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        
        boolean isListenerRegistered(String packageName) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(SERVICE_INTERFACE_TOKEN);
                data.writeString(packageName);
                
                mRemote.transact(TRANSACTION_IS_REGISTERED, data, reply, 0);
                reply.readException();
                return reply.readInt() != 0;
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        
        String getActiveListenerPackage() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(SERVICE_INTERFACE_TOKEN);
                
                mRemote.transact(TRANSACTION_GET_ACTIVE, data, reply, 0);
                reply.readException();
                return reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
    }
    
    private void registerListener() throws Exception {
        if (mService == null || mListenerBinder == null) {
            throw new IllegalStateException("Service or listener not initialized");
        }
        
        ServiceProxy proxy = (ServiceProxy) mService;
        String packageName = mContext.getPackageName();
        
        boolean result = proxy.registerTouchListener(mListenerBinder, packageName);
        if (!result) {
            throw new RuntimeException("Failed to register touch listener");
        }
        
        Log.d(TAG, "Successfully registered for touch events");
    }
    
    private void unregisterListener() {
        if (mService == null || mListenerBinder == null || !isConnected) {
            return;
        }
        
        try {
            ServiceProxy proxy = (ServiceProxy) mService;
            proxy.unregisterTouchListener(mListenerBinder);
            Log.d(TAG, "Unregistered touch listener");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister listener", e);
        }
    }
    
    /**
     * Remove the overlay view and clean up resources
     */
    public void destroy() {
        // Mark as disconnected first
        isConnected = false;
        
        // Unregister from service
        unregisterListener();
        
        // Clean up
        mService = null;
        mListenerBinder = null;
        touchListener = null;
        
        // Clear the active instance if it's this one
        if (activeInstance == this) {
            activeInstance = null;
        }
        
        Log.d(TAG, "Touch handler destroyed successfully");
    }
    
    /**
     * Check if the handler is active
     */
    public boolean isActive() {
        if (!isConnected || mService == null) {
            return false;
        }
        
        try {
            ServiceProxy proxy = (ServiceProxy) mService;
            return proxy.isListenerRegistered(mContext.getPackageName());
        } catch (Exception e) {
            Log.e(TAG, "Failed to check registration status", e);
            return false;
        }
    }
    
    /**
     * Update the touch listener
     */
    public void setOnTouchListener(OnTouchListener listener) {
        this.touchListener = listener;
    }
    
    /**
     * Get the package name of the currently active listener (for debugging)
     */
    public String getActiveListenerPackage() {
        if (!isConnected || mService == null) {
            return null;
        }
        
        try {
            ServiceProxy proxy = (ServiceProxy) mService;
            return proxy.getActiveListenerPackage();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get active listener package", e);
            return null;
        }
    }
}