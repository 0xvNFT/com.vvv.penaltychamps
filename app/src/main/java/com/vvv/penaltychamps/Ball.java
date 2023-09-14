package com.vvv.penaltychamps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Ball {
    private final Bitmap bitmap;
    private int x, y;

    public Ball(Context context) {
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ball);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}

