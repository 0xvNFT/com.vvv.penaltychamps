package com.vvv.penaltychamps;

public class ScoreManager {
    private int score;

    public ScoreManager() {
        this.score = 0;
    }

    public void increment() {
        this.score++;
    }

    public void reset() {
        this.score = 0;
    }

    public int getScore() {
        return this.score;
    }
}

