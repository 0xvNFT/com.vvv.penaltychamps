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
        scores[playerIndex]++;
        if (playerIndex == 0) {
            player1Actions.add(action);
        } else {
            player2Actions.add(action);
        }
    }

    public int getScore(int playerIndex) {
        return scores[playerIndex];
    }
}

