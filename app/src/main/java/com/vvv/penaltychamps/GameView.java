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
    private final Goalkeeper goalkeeper;
    private final ScoreManager scoreManager;
    private int goalPostX;
    private int goalPostY;
    private Thread gameThread;
    private boolean isPlaying;
    private Canvas canvas;
    private Bitmap backgroundImage;
    private boolean ballKicked = false;
    private Integer selectedHotspotIndex = -1;
    private boolean scoreUpdated = false;
    private PlayerRole currentPlayerRole = PlayerRole.SHOOTER;
    private boolean showAllHotspots = false;
    private final Bitmap backBuffer;
    private final Canvas backBufferCanvas;
    private boolean requireNewTouchForGoalkeeper = true;
    public GameView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        paint = new Paint();
        scoreManager = new ScoreManager();

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

        backBuffer = Bitmap.createBitmap(screenX, screenY, Bitmap.Config.ARGB_8888);
        backBufferCanvas = new Canvas(backBuffer);
        setGamePositions();
        initializeHotspots();
        //logHotspotPositions();
    }

    private void setGamePositions() {
        ball.setX(screenX / 2 - ball.getBitmap().getWidth() / 2 - 20);
        ball.setY(screenY - ball.getBitmap().getHeight() - 100);

        goalkeeper.setX(screenX / 2 - goalkeeper.getCurrentBitmap().getWidth() / 2 - 20);
        goalkeeper.setY(screenY / 2 - goalkeeper.getCurrentBitmap().getHeight() / 2);

        goalPostX = ((screenX / 2 - goalPostWidth / 2) - 20 + 3);
        goalPostY = (screenY / 2 - goalPostHeight / 2) - 65;
    }

    private void initializeHotspots() {
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
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                synchronized (lock) {
                    if (currentPlayerRole == PlayerRole.SHOOTER) {
                        handleShooterTouch(x, y);
                    }
                    if (currentPlayerRole == PlayerRole.GOALKEEPER) {
                        handleGoalkeeperTouch(x, y);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void handleShooterTouch(int x, int y) {
        selectedHotspotIndex = -1;
        for (int i = 0; i < hotspots.length; i++) {
            if (hotspots[i].contains(x, y)) {
                selectedHotspotIndex = i;
                ball.setHotspotIndex(i);
                kickBallTowards(i);
                moveKeeperRandomly();
                scoreUpdated = false;
                break;
            }
        }
    }

    private void handleGoalkeeperTouch(int x, int y) {
        selectedHotspotIndex = -1;
        for (int i = 0; i < hotspots.length; i++) {
            if (hotspots[i].contains(x, y)) {
                selectedHotspotIndex = i;
                goalkeeper.setHotspotIndex(i);
                requireNewTouchForGoalkeeper = false;
                resetBallPosition();
                kickBallTowardsRandom();
                moveGoalkeeperToHotspot();
                scoreUpdated = false;
                break;
            }
        }
    }


    private void update() {
        synchronized (lock) {
            int newX = ball.getX() + ball.getVelocityX();
            int newY = ball.getY() + ball.getVelocityY();
            if (ballKicked) {
                if (currentPlayerRole == PlayerRole.SHOOTER) {
                    updateShooter(newX, newY);
                }
                if (currentPlayerRole == PlayerRole.GOALKEEPER) {
                    updateGoalkeeper();
                }
            }
        }
    }

    private void updateShooter(int newX, int newY) {
        float scale = 1.0f;

        for (Rect hotspot : hotspots) {
            if (selectedHotspotIndex != null && selectedHotspotIndex >= 0 && selectedHotspotIndex < hotspots.length) {
                if (hotspots[selectedHotspotIndex].contains(newX, newY)) {
                    ball.setVelocity(0, 0);
                    if (!scoreUpdated) {
                        if (selectedHotspotIndex != goalkeeper.getHotspotIndex()) {
                            scoreManager.increment();
                        }
                        scoreUpdated = true;
                    }
                    switchRoles();
                    return;
                }
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

    private void updateGoalkeeper() {
        moveGoalkeeperToHotspot();
        if (requireNewTouchForGoalkeeper) {
            return;
        }
        if (selectedHotspotIndex != -1 && !ballKicked) {
            kickBallTowardsRandom();
            ballKicked = true;
        }

        if (ballKicked) {
            float scale = 1.0f;
            int newX = ball.getX() + ball.getVelocityX();
            int newY = ball.getY() + ball.getVelocityY();

            for (Rect hotspot : hotspots) {
                int targetX = hotspot.centerX();
                int targetY = hotspot.centerY();
                int dx = targetX - ball.getX();
                int dy = targetY - ball.getY();

                double distance = Math.sqrt(dx * dx + dy * dy);
                float tempScale = (float) (1.0 - (distance / 500.0));
                if (tempScale < 0.5) tempScale = 0.5f;
                if (tempScale < scale) scale = tempScale;

                if (hotspot.contains(newX, newY)) {
                    ball.setVelocity(0, 0);
                    ballKicked = false;
                    selectedHotspotIndex = -1;
                    switchRoles();
                    return;
                }
            }


            ball.setScale(scale);
            ball.setX(newX);
            ball.setY(newY);
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

    private void draw() {
        synchronized (lock) {
            if (surfaceHolder.getSurface().isValid()) {
                try {
                    backBufferCanvas.drawBitmap(backgroundImage, 0, 0, null);
                    backBufferCanvas.drawBitmap(goalkeeper.getCurrentBitmap(), goalkeeper.getX(), goalkeeper.getY(), null);

                    backBufferCanvas.drawBitmap(Bitmap.createScaledBitmap(ball.getBitmap(),
                                    (int) (ball.getBitmap().getWidth() * ball.getScale()),
                                    (int) (ball.getBitmap().getHeight() * ball.getScale()), false),
                            ball.getX(), ball.getY(), null);

                    paint.setColor(Color.WHITE);
                    paint.setTextSize(50);
                    backBufferCanvas.drawText("ScoreManager: " + scoreManager.getScore(), 50, 50, paint);

                    paint.setTextSize(50);
                    backBufferCanvas.drawText("Current Role: " + currentPlayerRole.toString(), 50, 100, paint);

                    paint.setColor(Color.TRANSPARENT);
                    int goalPostRight = goalPostX + goalPostWidth;
                    int goalPostBottom = goalPostY + goalPostHeight;
                    backBufferCanvas.drawRect(goalPostX, goalPostY, goalPostRight, goalPostBottom, paint);

                    paint.setColor(Color.BLUE);
                    if (currentPlayerRole == PlayerRole.GOALKEEPER && showAllHotspots) {
                        for (Rect hotspot : hotspots) {
                            backBufferCanvas.drawRect(hotspot, paint);
                        }
                    } else if (selectedHotspotIndex >= 0 && selectedHotspotIndex < hotspots.length) {
                        backBufferCanvas.drawRect(hotspots[selectedHotspotIndex], paint);
                    } else {
                        for (Rect hotspot : hotspots) {
                            backBufferCanvas.drawRect(hotspot, paint);
                        }
                    }

                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawBitmap(backBuffer, 0, 0, null);
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private void moveKeeperRandomly() {
        if (currentPlayerRole == PlayerRole.SHOOTER) {
            int randomHotspotIndex = new Random().nextInt(hotspots.length);
            goalkeeper.setBitmapForAction(randomHotspotIndex, hotspots[randomHotspotIndex]);
            goalkeeper.setHotspotIndex(randomHotspotIndex);
        }
    }

    private void kickBallTowardsRandom() {
        synchronized (lock) {
            int randomHotspotIndex = new Random().nextInt(hotspots.length);

            int targetX = hotspots[randomHotspotIndex].centerX();
            int targetY = hotspots[randomHotspotIndex].centerY();

            int dx = targetX - ball.getX();
            int dy = targetY - ball.getY();

            double length = Math.sqrt(dx * dx + dy * dy);
            int velocityX = (int) (dx / length * 10);
            int velocityY = (int) (dy / length * 10);

            ball.setVelocity(velocityX, velocityY);
            ballKicked = true;
        }
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
            ballKicked = true;
        }
    }

    private void switchRoles() {
        if (currentPlayerRole == PlayerRole.SHOOTER) {
            currentPlayerRole = PlayerRole.GOALKEEPER;
            showAllHotspots = true;
            requireNewTouchForGoalkeeper = true;
        } else {
            currentPlayerRole = PlayerRole.SHOOTER;
            showAllHotspots = true;
        }

        resetBallPosition();
        resetGoalkeeperPosition();
        ballKicked = false;
        scoreUpdated = false;
    }

    public void resetBallPosition() {
        ball.setX(screenX / 2 - ball.getBitmap().getWidth() / 2 - 20);
        ball.setY(screenY - ball.getBitmap().getHeight() - 100);
        ball.setScale(1.0f);
    }
    public void resetGoalkeeperPosition() {
        goalkeeper.setHotspotIndex(7);
        goalkeeper.setCurrentBitmap();
        goalkeeper.setX(screenX / 2 - goalkeeper.getCurrentBitmap().getWidth() / 2 - 20);
        goalkeeper.setY(screenY - goalkeeper.getCurrentBitmap().getHeight() - 350);

    }

    private void moveGoalkeeperToHotspot() {
        goalkeeper.setBitmapForAction(goalkeeper.getHotspotIndex(), hotspots[goalkeeper.getHotspotIndex()]);
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

    private enum PlayerRole {
        SHOOTER,
        GOALKEEPER
    }
}