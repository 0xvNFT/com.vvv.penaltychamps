package com.vvv.penaltychamps;

import java.util.ArrayList;

public class ScoreManager {
    private final int[] scores;
    public final ArrayList<String> player1Actions, player2Actions;

    public ScoreManager(int playerCount) {
        scores = new int[playerCount];
        player1Actions = new ArrayList<>();
        player2Actions = new ArrayList<>();
    }

    public void increment(int playerIndex, String action) {
        if ("goal".equals(action)) {
            scores[playerIndex]++;
        }
        if (playerIndex == 0) {
            player1Actions.add(action);
        } else {
            player2Actions.add(action);
        }
    }

    public boolean isBestOfFiveMet() {
        return scores[0] >= 5 || scores[1] >= 5;
    }

    public int getWinner() {
        if (scores[0] >= 5) return 0;
        if (scores[1] >= 5) return 1;
        return -1;
    }

    public boolean canCatchUp() {
        int scoreDiff = Math.abs(scores[0] - scores[1]);
        int remainingRounds = 5 - Math.max(scores[0], scores[1]);

        return scoreDiff <= remainingRounds;
    }


    public int getScore(int playerIndex) {
        return scores[playerIndex];
    }
}

