package com.vvv.penaltychamps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private final SurfaceHolder surfaceHolder;
    private boolean isPlaying;
    private Canvas canvas;
    private final Paint paint;
    private final Object lock = new Object();
    private int score;
    private final Ball ball;
    private final int screenX;
    private final int screenY;
    private float initialTouchX, initialTouchY;
    private boolean dragging = false;
    private Bitmap backgroundImage;

    public GameView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        paint = new Paint();

        ball = new Ball(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenX = point.x;
        screenY = point.y;

        backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.ingame_bg);
        backgroundImage = Bitmap.createScaledBitmap(backgroundImage, screenX, screenY, false);

        ball.setX(screenX / 2 - ball.getBitmap().getWidth() / 2);
        ball.setY(screenY - ball.getBitmap().getHeight() - 100);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                synchronized (lock) {
                    score++;
                }
                float touchX = event.getX();
                float touchY = event.getY();
                if (touchX >= ball.getX() && touchX <= (ball.getX() + ball.getBitmap().getWidth()) &&
                        touchY >= ball.getY() && touchY <= (ball.getY() + ball.getBitmap().getHeight())) {
                    dragging = true;
                    initialTouchX = touchX;
                    initialTouchY = touchY;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    float moveX = event.getX() - initialTouchX;
                    float moveY = event.getY() - initialTouchY;
                    ball.setX(ball.getX() + (int) moveX);
                    ball.setY(ball.getY() + (int) moveY);
                    initialTouchX = event.getX();
                    initialTouchY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                dragging = false;
                ball.setX(screenX / 2 - ball.getBitmap().getWidth() / 2);
                ball.setY(screenY - ball.getBitmap().getHeight() - 100);
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
        synchronized (lock) {
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            try {
                canvas = surfaceHolder.lockCanvas();

                //canvas.drawColor(Color.BLACK);

                if (canvas != null) {
                    canvas.drawBitmap(backgroundImage, 0, 0, null);
                    canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), null);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
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

