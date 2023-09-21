package com.vvv.penaltychamps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Goalkeeper {
    private final Bitmap stillBitmap;
    private final Bitmap bottomMiddleLeftBitmap;
    private final Bitmap bottomMiddleRightBitmap;
    private final Bitmap topLeftBitmap;
    private final Bitmap topRightBitmap;
    private final Bitmap middleBitmap;
    private Bitmap currentBitmap;
    private int x, y;
    private int targetX, targetY;
    int hotspotIndex;

    public Goalkeeper(Context context) {
        stillBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gk_still);
        bottomMiddleLeftBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gk_bottom_middle_left);
        bottomMiddleRightBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gk_bottom_middle_right);
        topLeftBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gk_top_left);
        topRightBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gk_top_right);
        middleBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gk_middle);
        currentBitmap = stillBitmap;
    }

    public void setBitmapForAction(int hotspotIndex, Rect hotspot) {
        switch (hotspotIndex) {
            case 0:
            case 6:
                currentBitmap = topLeftBitmap;
                break;
            case 1:
                currentBitmap = topRightBitmap;
                break;
            case 2:
            case 4:
                currentBitmap = bottomMiddleLeftBitmap;
                break;
            case 3:
            case 5:
                currentBitmap = bottomMiddleRightBitmap;
                break;
            case 7:
            case 8:
                currentBitmap = middleBitmap;
                break;
        }
        this.targetX = hotspot.centerX();
        this.targetY = hotspot.centerY();

        this.x = targetX - currentBitmap.getWidth() / 2;
        this.y = targetY - 50;
    }

    public int getHotspotIndex() {
        return hotspotIndex;
    }

    public void setHotspotIndex(int hotspotIndex) {
        this.hotspotIndex = hotspotIndex;
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

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public void setCurrentBitmap() {
        currentBitmap = stillBitmap;
    }
}
