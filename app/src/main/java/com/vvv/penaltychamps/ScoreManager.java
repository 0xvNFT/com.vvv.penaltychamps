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

    public boolean isBestOfFiveMet() {
        int player1Score = getScore(0);
        int player2Score = getScore(1);

        // Criteria for Best of Five
        if (player1Actions.size() >= 5 && player2Actions.size() >= 5) {
            int scoreDiff = Math.abs(player1Score - player2Score);

            // Catch-up logic: if the difference is 1, continue the game
            if (scoreDiff == 1) {
                return false;
            }

            // If difference is 2 or more, game over
            return scoreDiff >= 2;
        }
        return false;
    }

    public int getWinner() {
        int player1Score = getScore(0);
        int player2Score = getScore(1);
        if (player1Score > player2Score) {
            return 0; // Player 1 wins
        } else if (player2Score > player1Score) {
            return 1; // Player 2 wins
        } else {
            return -1; // Draw
        }
    }


    public int getScore(int playerIndex) {
        return scores[playerIndex];
    }
}

