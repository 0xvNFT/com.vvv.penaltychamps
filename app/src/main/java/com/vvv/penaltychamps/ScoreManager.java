package com.vvv.penaltychamps;

public class ScoreManager {
    private final int[] scores;

    public ScoreManager(int playerCount) {
        scores = new int[playerCount];
    }

    public void increment(int playerIndex) {
        scores[playerIndex]++;
    }

    public int getScore(int playerIndex) {
        return scores[playerIndex];
    }
}

