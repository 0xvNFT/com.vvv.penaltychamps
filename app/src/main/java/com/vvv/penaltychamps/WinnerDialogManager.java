package com.vvv.penaltychamps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class WinnerDialogManager {

    private final Context context;

    public WinnerDialogManager(Context context) {
        this.context = context;
    }

    public void showWinnerDialog(String winner, int p1Score, int p2Score) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_winner, null);

        TextView tvWinner = view.findViewById(R.id.tv_winner);
        TextView tvScore = view.findViewById(R.id.tv_score);
        Button btnClose = view.findViewById(R.id.btn_close);

        tvWinner.setText("Winner: " + winner);
        tvScore.setText("Score: " + p1Score + " - " + p2Score);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

