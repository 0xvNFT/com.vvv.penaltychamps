package com.vvv.penaltychamps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private final SurfaceHolder surfaceHolder;
    private boolean isPlaying;
    private Canvas canvas;
    private final Paint paint;

    public GameView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        paint = new Paint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }


    @Override
    public void run() {
        long targetTime = 1000 / 60;
        long startTime, waitTime, elapsedTime;

        while (isPlaying) {
            startTime = System.nanoTime();

            draw();
            update();

            elapsedTime = System.nanoTime() - startTime;
            waitTime = targetTime - elapsedTime / 1000000;

            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void update() {
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

