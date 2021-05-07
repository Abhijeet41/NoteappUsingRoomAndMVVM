package com.abhi41.noteapproomdb.UI.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class ImageConverter {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        //Setting the width and height of the Bitmap that will be returned
        // equal to the original Bitmap that needs round corners.
        Bitmap output = null;
        if (bitmap != null){
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        }

        //Creating canvas with output Bitmap for drawing
        Canvas canvas = new Canvas(output);

        //Setting paint and rectangles.
        final int color = Color.BLACK;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        //SetXfermode applies PorterDuffXfermode to paint.
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
