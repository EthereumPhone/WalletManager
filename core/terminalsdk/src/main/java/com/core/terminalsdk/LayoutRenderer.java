package com.core.terminalsdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import android.graphics.PorterDuff;
import android.widget.ImageView;
import android.view.ViewGroup;

public class LayoutRenderer {

    private Context context;

    public LayoutRenderer(Context context) {
        this.context = context;
    }

    /**
     * Renders the qr_or_send with custom text into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderQrOrSend() {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.qr_or_send, null);

        // Apply accent color to all interactive elements (icons + labels)
        int accentColor = getColorForRender();

        // Re-tint icons
        ImageView scanIcon = view.findViewById(R.id.scan_icon);
        if (scanIcon != null) {
            scanIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }
        ImageView sendIcon = view.findViewById(R.id.send_icon);
        if (sendIcon != null) {
            sendIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        // Re-color labels
        TextView scanLabel = view.findViewById(R.id.scan_label);
        if (scanLabel != null) {
            scanLabel.setTextColor(accentColor);
        }
        TextView sendLabel = view.findViewById(R.id.send_label);
        if (sendLabel != null) {
            sendLabel.setTextColor(accentColor);
        }

        // Measure and layout the view with exact dimensions (428x142 pixels)
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        // Create a bitmap with the exact dimensions
        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);

        // Create a canvas to draw the view onto the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the view onto the canvas
        view.draw(canvas);

        return bitmap;
    }


    /**
     * Renders the copy layout with custom text into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderCopy() {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.copy_terminal_layout, null);

        int accentColor = getColorForRender();

        //Tint icons
        ImageView copyIcon = view.findViewById(R.id.copy_icon);
        if (copyIcon != null) {
            copyIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        // Re-color labels
        TextView copyLabel = view.findViewById(R.id.copy_label);
        if (copyLabel != null) {
            copyLabel.setTextColor(accentColor);
        }

        // Measure and layout the view with exact dimensions (428x142 pixels)
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        // Create a bitmap with the exact dimensions
        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);

        // Create a canvas to draw the view onto the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the view onto the canvas
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Renders the topUp with custom text into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderTopUp() {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.topup_terminal_layout, null);

        int accentColor = getColorForRender();

        //Tint icons
        ImageView copyIcon = view.findViewById(R.id.topup_icon);
        if (copyIcon != null) {
            copyIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        // Re-color labels
        TextView copyLabel = view.findViewById(R.id.topup_label);
        if (copyLabel != null) {
            copyLabel.setTextColor(accentColor);
        }

        // Measure and layout the view with exact dimensions (428x142 pixels)
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        // Create a bitmap with the exact dimensions
        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);

        // Create a canvas to draw the view onto the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the view onto the canvas
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Renders the topUp with custom text into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderLogTerminal() {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.log_terminal_layout, null);

        int accentColor = getColorForRender();

        //Tint icons
        ImageView copyIcon = view.findViewById(R.id.log_icon);
        if (copyIcon != null) {
            copyIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        // Re-color labels
        TextView copyLabel = view.findViewById(R.id.log_label);
        if (copyLabel != null) {
            copyLabel.setTextColor(accentColor);
        }

        // Measure and layout the view with exact dimensions (428x142 pixels)
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        // Create a bitmap with the exact dimensions
        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);

        // Create a canvas to draw the view onto the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the view onto the canvas
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Iterate over all pixels and set any pixel that is not close to black to the provided color.
     * @param bmp         The bitmap to manipulate.
     * @param toColor     The color to apply to non-black pixels.
     * @param tolerance   Maximum value (0-255) that R, G, and B can have while still being considered black.
     */
    private void replaceNonBlack(Bitmap bmp, int toColor, int tolerance) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int px = pixels[i];
            int r = Color.red(px);
            int g = Color.green(px);
            int b = Color.blue(px);

            // If any channel is above the tolerance, treat as non-black
            if (r > tolerance || g > tolerance || b > tolerance) {
                pixels[i] = toColor;
            }
        }

        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    /**
     * Renders a black layout with the given text in RED and centered on the bitmap.
     * The rendered bitmap size is 428x142 px – identical to the mini-display FrameLayout.
     *
     * @param text The text to be rendered on the layout.
     * @return A bitmap containing the rendered layout.
     */
    public Bitmap renderBlackText(String text) {
        // Inflate the black layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.black_layout, null);

        // Try to set the text on the TextView inside the layout (expected id: messageText)
        TextView tv = view.findViewById(R.id.messageText);
        if (tv != null) {
            tv.setText(text);
            Typeface typeface = ResourcesCompat.getFont(context, R.font.monomaniac);
            tv.setTypeface(typeface);
            tv.setTextColor(getColorForRender());
            tv.setGravity(Gravity.CENTER);
        }

        // Measure & layout the view (fixed 428 × 142 px)
        int width = 428;
        int height = 142;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, width, height);

        // Create bitmap and draw the view onto it
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private int getColorForRender() {
        return Settings.Secure.getInt(
                context.getContentResolver(),
                "systemui_accent_color",
                0xFFFE0000  // Default red
        );
    }
}