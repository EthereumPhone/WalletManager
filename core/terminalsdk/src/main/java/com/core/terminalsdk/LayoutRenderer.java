package com.core.terminalsdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
     * Helper method to save the bitmap to a file if needed
     * @param bitmap The bitmap to save
     * @param filename The filename (without extension)
     * @return true if saved successfully, false otherwise
     */


}