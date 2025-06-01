package com.core.terminalsdk;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;



public class MiniDisplayTouchHandler {
    private static final String TAG = "MiniDisplayTouchHandler";
    private static final int MINI_DISPLAY_ID = 6969001;

    private WindowManager windowManager;
    private View touchCatcher;
    private Context displayContext;
    private OnTouchListener touchListener;

    // Callback interface for touch events
    public interface OnTouchListener {
        void onTouch(float x, float y, int action);
    }

    public MiniDisplayTouchHandler(Context context, OnTouchListener listener) {
        this.touchListener = listener;
        initializeMiniDisplay(context);
    }

    private void initializeMiniDisplay(Context context) {
        // Get the DisplayManager
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display miniDisplay = displayManager.getDisplay(MINI_DISPLAY_ID);

        if (miniDisplay == null) {
            Log.e(TAG, "Mini display not found with ID: " + MINI_DISPLAY_ID);
            throw new IllegalStateException("Mini display not found");
        }

        // Create a Context bound to the mini display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            displayContext = context.createDisplayContext(miniDisplay);
        } else {
            throw new UnsupportedOperationException("Mini display requires API 17+");
        }

        // Get WindowManager for the display context
        windowManager = (WindowManager) displayContext.getSystemService(Context.WINDOW_SERVICE);

        // Create transparent view to catch touches
        touchCatcher = new View(displayContext);
        touchCatcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (touchListener != null) {
                    touchListener.onTouch(event.getX(), event.getY(), event.getAction());

                    // Log for debugging
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.d(TAG, "Touch at " + event.getX() + "," + event.getY());
                    }
                }
                return true;
            }
        });

        // Set up layout parameters for overlay
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        // Add the view to the window
        try {
            windowManager.addView(touchCatcher, layoutParams);
            Log.d(TAG, "Touch handler initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to add overlay view", e);
            throw new RuntimeException("Failed to add overlay view", e);
        }
    }

    /**
     * Remove the overlay view and clean up resources
     */
    public void destroy() {
        if (windowManager != null && touchCatcher != null) {
            try {
                windowManager.removeView(touchCatcher);
                Log.d(TAG, "Touch handler destroyed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error removing overlay view", e);
            }
        }
        touchCatcher = null;
        windowManager = null;
        displayContext = null;
        touchListener = null;
    }

    /**
     * Check if the handler is active
     */
    public boolean isActive() {
        return touchCatcher != null && touchCatcher.getParent() != null;
    }

    /**
     * Update the touch listener
     */
    public void setOnTouchListener(OnTouchListener listener) {
        this.touchListener = listener;
    }
}