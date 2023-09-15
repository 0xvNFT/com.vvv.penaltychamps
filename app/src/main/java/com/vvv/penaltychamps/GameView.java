package com.vvv.penaltychamps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
    private Bitmap backgroundImage;
    private final Rect[] hotspots = new Rect[9];
    private final int goalPostWidth = 300;
    private final int goalPostHeight = 120;
    private final int goalPostX;
    private final int goalPostY;
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
        int rectWidth = screenX / 3;
        int rectHeight = screenY / 6;

        goalPostX = ((screenX / 2 - goalPostWidth / 2) - 20 + 3);
        goalPostY = (screenY / 2 - goalPostHeight / 2) - 65;

        int distanceFromGoal = 30;
        for (int i = 0; i < hotspots.length; i++) {
            int left, top, right, bottom;
            switch (i) {
                case 0:
                    // Top-left corner
                    left = goalPostX - distanceFromGoal;
                    top = goalPostY - distanceFromGoal;
                    break;
                case 1:
                    // Top-right corner
                    left = goalPostX + goalPostWidth;
                    top = goalPostY - distanceFromGoal;
                    break;
                case 2:
                    // Bottom-left corner
                    left = goalPostX - distanceFromGoal;
                    top = goalPostY + goalPostHeight;
                    break;
                case 3:
                    // Bottom-right corner
                    left = goalPostX + goalPostWidth;
                    top = goalPostY + goalPostHeight;
                    break;
                case 4:
                    // Middle-left
                    left = goalPostX - distanceFromGoal;
                    top = goalPostY + goalPostHeight / 2 - distanceFromGoal / 2;
                    break;
                case 5:
                    // Middle-right
                    left = goalPostX + goalPostWidth;
                    top = goalPostY + goalPostHeight / 2 - distanceFromGoal / 2;
                    break;
                case 6:
                    // Top-middle
                    left = goalPostX + goalPostWidth / 2 - distanceFromGoal / 2;
                    top = goalPostY - distanceFromGoal;
                    break;
                case 7:
                    // Middle
                    left = goalPostX + goalPostWidth / 2 - distanceFromGoal / 2;
                    top = goalPostY + goalPostHeight / 2 - distanceFromGoal / 2;
                    break;
                case 8:
                    // Bottom-middle
                    left = goalPostX + goalPostWidth / 2 - distanceFromGoal / 2;
                    top = goalPostY + goalPostHeight;
                    break;
                default:
                    left = 0;
                    top = 0;
                    break;
            }
            right = left + distanceFromGoal;
            bottom = top + distanceFromGoal;
            hotspots[i] = new Rect(left, top, right, bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                synchronized (lock) {
                    for (int i = 0; i < hotspots.length; i++) {
                        if (hotspots[i].contains(x, y)) {
                            kickBallTowards(i);
                            break;
                        }
                    }
                    score++;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void kickBallTowards(int hotspotIndex) {
        synchronized (lock) {
            int targetX = hotspots[hotspotIndex].centerX();
            int targetY = hotspots[hotspotIndex].centerY();

            int dx = targetX - ball.getX();
            int dy = targetY - ball.getY();

            double length = Math.sqrt(dx * dx + dy * dy);
            int velocityX = (int) (dx / length * 10);
            int velocityY = (int) (dy / length * 10);

            ball.setVelocity(velocityX, velocityY);
        }
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
            int newX = ball.getX() + ball.getVelocityX();
            int newY = ball.getY() + ball.getVelocityY();

            if (newX < 0) newX = 0;
            if (newX > screenX - ball.getBitmap().getWidth())
                newX = screenX - ball.getBitmap().getWidth();
            if (newY < 0) newY = 0;
            if (newY > screenY - ball.getBitmap().getHeight())
                newY = screenY - ball.getBitmap().getHeight();

            ball.setX(newX);
            ball.setY(newY);
        }
    }


    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            try {
                canvas = surfaceHolder.lockCanvas();

                if (canvas != null) {
                    canvas.drawBitmap(backgroundImage, 0, 0, null);
                    canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), null);

                    paint.setColor(Color.CYAN);
                    int goalPostRight = goalPostX + goalPostWidth;
                    int goalPostBottom = goalPostY + goalPostHeight;
                    canvas.drawRect(goalPostX, goalPostY, goalPostRight, goalPostBottom, paint);

                    paint.setColor(Color.RED);
                    for (Rect hotspot : hotspots) {
                        canvas.drawRect(hotspot, paint);
                    }
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

