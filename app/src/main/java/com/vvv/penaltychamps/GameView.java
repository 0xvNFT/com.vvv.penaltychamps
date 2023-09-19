package com.vvv.penaltychamps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private final SurfaceHolder surfaceHolder;
    private final Paint paint;
    private final Object lock = new Object();
    private final Ball ball;
    private final int screenX;
    private final int screenY;
    private final Rect[] hotspots = new Rect[9];
    private final int goalPostWidth = 250;
    private final int goalPostHeight = 100;
    private final int goalPostX;
    private final int goalPostY;
    private final Goalkeeper goalkeeper;
    private Thread gameThread;
    private boolean isPlaying;
    private Canvas canvas;
    private int score;
    private Bitmap backgroundImage;
    private boolean ballKicked = false;
    private Integer selectedHotspotIndex = null;
    public GameView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        paint = new Paint();

        ball = new Ball(context);
        goalkeeper = new Goalkeeper(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenX = point.x;
        screenY = point.y;

        backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.ingame_bg);
        backgroundImage = Bitmap.createScaledBitmap(backgroundImage, screenX, screenY, false);

        ball.setX(screenX / 2 - ball.getBitmap().getWidth() / 2 - 20);
        ball.setY(screenY - ball.getBitmap().getHeight() - 100);

        goalkeeper.setX(screenX / 2 - goalkeeper.getCurrentBitmap().getWidth() / 2 - 20);
        goalkeeper.setY(screenY / 2 - goalkeeper.getCurrentBitmap().getHeight() / 2);

        int rectWidth = screenX / 3;
        int rectHeight = screenY / 6;

        goalPostX = ((screenX / 2 - goalPostWidth / 2) - 20 + 3);
        goalPostY = (screenY / 2 - goalPostHeight / 2) - 65;

        int distanceFromGoal = 40;
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
        logHotspotPositions();

    }

    @SuppressLint("DefaultLocale")
    private void logHotspotPositions() {
        StringBuilder logMessage = new StringBuilder("Hotspot Positions:\n");
        for (int i = 0; i < hotspots.length; i++) {
            Rect hotspot = hotspots[i];
            logMessage.append(String.format("Hotspot %d: Left: %d, Top: %d, Right: %d, Bottom: %d\n",
                    i, hotspot.left, hotspot.top, hotspot.right, hotspot.bottom));
        }
        Log.d("GameView", logMessage.toString());
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
                            selectedHotspotIndex = i;
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
            int randomHotspotIndex = new Random().nextInt(hotspots.length);
            goalkeeper.setBitmapForAction(randomHotspotIndex, hotspots[randomHotspotIndex]);

            int targetX = hotspots[hotspotIndex].centerX();
            int targetY = hotspots[hotspotIndex].centerY();

            int dx = targetX - ball.getX();
            int dy = targetY - ball.getY();

            double length = Math.sqrt(dx * dx + dy * dy);
            int velocityX = (int) (dx / length * 10);
            int velocityY = (int) (dy / length * 10);

            ball.setVelocity(velocityX, velocityY);
            ballKicked = true;
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

            if (ballKicked) {
                float scale = 1.0f;

                for (Rect hotspot : hotspots) {
                    if (selectedHotspotIndex != null && hotspots[selectedHotspotIndex].contains(newX, newY)) {
                        ball.setVelocity(0, 0);
                        return;
                    }

                    int targetX = hotspot.centerX();
                    int targetY = hotspot.centerY();
                    int dx = targetX - ball.getX();
                    int dy = targetY - ball.getY();

                    double distance = Math.sqrt(dx * dx + dy * dy);
                    float tempScale = (float) (1.0 - (distance / 500.0));
                    if (tempScale < 0.5) tempScale = 0.5f;

                    if (tempScale < scale) scale = tempScale;
                }

                ball.setScale(scale);

                if (newX < 0) newX = 0;
                if (newX > screenX - ball.getBitmap().getWidth() * scale)
                    newX = (int) (screenX - ball.getBitmap().getWidth() * scale);
                if (newY < 0) newY = 0;
                if (newY > screenY - ball.getBitmap().getHeight() * scale)
                    newY = (int) (screenY - ball.getBitmap().getHeight() * scale);

                ball.setX(newX);
                ball.setY(newY);
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            try {
                canvas = surfaceHolder.lockCanvas();

                if (canvas != null) {
                    canvas.drawBitmap(backgroundImage, 0, 0, null);
                    canvas.drawBitmap(goalkeeper.getCurrentBitmap(), goalkeeper.getX(), goalkeeper.getY(), null);

                    canvas.drawBitmap(Bitmap.createScaledBitmap(ball.getBitmap(),
                                    (int) (ball.getBitmap().getWidth() * ball.getScale()),
                                    (int) (ball.getBitmap().getHeight() * ball.getScale()), false),
                            ball.getX(), ball.getY(), null);

                    paint.setColor(Color.TRANSPARENT);
                    int goalPostRight = goalPostX + goalPostWidth;
                    int goalPostBottom = goalPostY + goalPostHeight;
                    canvas.drawRect(goalPostX, goalPostY, goalPostRight, goalPostBottom, paint);

                    paint.setColor(Color.BLUE);
                    if (selectedHotspotIndex == null) {
                        for (Rect hotspot : hotspots) {
                            canvas.drawRect(hotspot, paint);
                        }
                    } else {
                        canvas.drawRect(hotspots[selectedHotspotIndex], paint);
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