package com.example.sudoku.GameBoard;

import static com.example.sudoku.GameBoard.GameData.originalGrid;
import static com.example.sudoku.GameBoard.SudokuGenerator.GRID_SIZE;
import static com.example.sudoku.GameBoard.SudokuGenerator.generateSudoku;
import static com.example.sudoku.GameBoard.SudokuGenerator.grid;
import static com.example.sudoku.GameBoard.SudokuGenerator.setupNumberButtons;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sudoku.HomePage;
import com.example.sudoku.R;
import com.google.android.material.button.MaterialButton;
import java.util.Arrays;
import java.util.Locale;
import java.util.Stack;

public class GameBoard extends AppCompatActivity implements View.OnClickListener {

    private static TextView mistakeCounterTextView; // TextView for displaying mistakes
    public static int mistakes = 0; // Count of mistakes made
    private static final int MAX_MISTAKES = 3; // Maximum allowed mistakes
    static MaterialButton[]  numberButtons;
    private ImageView timerimg;
    private ImageView restartimg;
    private ImageView erasevalue;
    private ImageView backtohome;
    private TextView timerTextView;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 600000; // Default: 10 minutes
    public static GridLayout sudokuBoard;
    public static boolean isGameActive = true;
    public static Stack<int[][]> moveStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboard_activity);

        sudokuBoard = findViewById(R.id.sudokuBoard);
        mistakeCounterTextView = findViewById(R.id.mistakecounter);
        timerimg = findViewById(R.id.timerimg);
        timerTextView = findViewById(R.id.timer);
        ImageView timeSelectImg = findViewById(R.id.timeselect);
        restartimg = findViewById(R.id.restartimg);
        erasevalue = findViewById(R.id.erasevalue);
        backtohome = findViewById(R.id.backtohome);
        moveStack = new Stack<>();



        ImageView undoImg = findViewById(R.id.undoimg);
        undoImg.setOnClickListener(v -> {
                undoLastMove();
        });

        timeSelectImg.setOnClickListener(v -> showTimeSelectionDialog());

        generateSudoku(sudokuBoard, this);

        numberButtons = new MaterialButton[9];
        numberButtons[0] = findViewById(R.id.num1);
        numberButtons[1] = findViewById(R.id.num2);
        numberButtons[2] = findViewById(R.id.num3);
        numberButtons[3] = findViewById(R.id.num4);
        numberButtons[4] = findViewById(R.id.num5);
        numberButtons[5] = findViewById(R.id.num6);
        numberButtons[6] = findViewById(R.id.num7);
        numberButtons[7] = findViewById(R.id.num8);
        numberButtons[8] = findViewById(R.id.num9);

        for (MaterialButton numberButton : numberButtons) {
            numberButton.setOnClickListener(this);
        }

        setupNumberButtons(numberButtons,this);


        // Set onClickListener to pause/resume the timer
        timerimg.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                resumeTimer();
            }
        });

        restartimg.setOnClickListener(v -> {
            generateSudoku(sudokuBoard, getApplicationContext());
        });

        erasevalue.setOnClickListener(v->{
            SudokuGenerator.eraseSelectedCell();
        });

        backtohome.setOnClickListener(v->{
            showConfirmDialog();
        });

    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < 9; i++) {
            if (v.getId() == numberButtons[i].getId()) {
                int number = i + 1; // Get the number based on the button clicked

                // Call the method to set the number in the selected cell
                SudokuGenerator.setNumberInSelectedCell(SudokuGenerator.selectedCell, number);
                break;
            }
        }
    }

    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(GameBoard.this);
        dialog.setContentView(R.layout.dialog_confirm_exit);
        dialog.setCancelable(true);

        Button okButton = dialog.findViewById(R.id.okButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(v -> {
            Intent i = new Intent(dialog.getContext(), HomePage.class);
            dialog.getContext().startActivity(i);
            finish();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTimeSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_timer_selection);

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
        Button setTimeBtn = dialog.findViewById(R.id.btn_set_time);

        setTimeBtn.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.radio_5sec) {
                timeLeftInMillis = 5000; // 5 minutes
                timerTextView.setText("00:05");
                timerimg.setEnabled(true);
                generateSudoku(sudokuBoard, getApplicationContext());
                startTimer();
            }
            else if (selectedId == R.id.radio_5min) {
                timeLeftInMillis = 300000; // 5 minutes
                timerTextView.setText("05:00");
                timerimg.setEnabled(true);
                generateSudoku(sudokuBoard, getApplicationContext());
                startTimer();
            } else if (selectedId == R.id.radio_10min) {
                timeLeftInMillis = 600000; // 10 minutes
                timerTextView.setText("10:00");
                generateSudoku(sudokuBoard, getApplicationContext());
                timerimg.setEnabled(true);
                startTimer();
            } else if (selectedId == R.id.radio_15min) {
                timeLeftInMillis = 900000; // 15 minutes
                timerTextView.setText("15:00");
                generateSudoku(sudokuBoard, getApplicationContext());
                timerimg.setEnabled(true);
                startTimer();
            } else if (selectedId == R.id.radio_no_time) {
                pauseTimer();
                timerTextView.setText("--");
                generateSudoku(sudokuBoard, getApplicationContext());
                timerimg.setEnabled(false);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    public void undoLastMove() {
        if (moveStack == null || moveStack.isEmpty()) {
            Log.d("SudokuGame", "No moves to undo.");
            Toast.makeText(this,"Undo is available for previous 2 moves only",Toast.LENGTH_SHORT).show();
            return; // Early return if no moves are available
        }

        // Pop the last grid state
        int[][] lastGridState = moveStack.pop();
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(lastGridState[i], 0, grid[i], 0, GRID_SIZE);
        }

        // Update the UI for all cells based on the restored grid state
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                TextView cellView = (TextView) sudokuBoard.getChildAt(i * GRID_SIZE + j);
                cellView.setText(grid[i][j] == 0 ? "" : String.valueOf(grid[i][j]));
            }
        }

        Log.d("SudokuGame", "Last move undone. Current state: " + Arrays.deepToString(grid));
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                // Timer finished, handle game over
                showTimeUpDialog();
            }
        }.start();

        isTimerRunning = true;
        timerimg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pause)); // Change to pause icon
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timerimg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.play)); // Change to play icon
    }

    private void resumeTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                // Timer finished, handle game over
            }
        }.start();

        isTimerRunning = true;
        timerimg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pause)); // Change to pause icon
    }

    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    public void showTimeUpDialog() {
        // Create a Dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_times_up);

        // Get references to UI elements
        Button btnClose = dialog.findViewById(R.id.btnClose);

        // Set click listener for the close button
        btnClose.setOnClickListener(v -> {
            Intent i = new Intent(dialog.getContext(), HomePage.class);
            startActivity(i);
            dialog.dismiss(); // Close the dialog
            // Optionally, reset the game or do any other action here
        });

        // Show the dialog
        dialog.setCancelable(false); // Prevent dismissing the dialog when touching outside
        dialog.show();
    }

    @SuppressLint("DefaultLocale")
    public static void updateMistakeCounter() {
        mistakeCounterTextView.setText(String.format("%d/%d", mistakes, MAX_MISTAKES)); // Update the display
        if (mistakes >= MAX_MISTAKES) {
            LayoutInflater inflater = LayoutInflater.from(mistakeCounterTextView.getContext());
            View dialogView = inflater.inflate(R.layout.custom_max_mistake_dialog, null);

            // Create and set up the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mistakeCounterTextView.getContext());
            builder.setView(dialogView);

            // Create the AlertDialog instance
            AlertDialog dialog = builder.create();

            // Get references to the buttons in the custom layout
            Button btnContinue = dialogView.findViewById(R.id.btnContinue);
            Button btnExit = dialogView.findViewById(R.id.btnExit);

            // Handle "Continue" button click
            btnContinue.setOnClickListener(v -> {
                // Reset mistake counter and update the mistake counter TextView
                mistakes = 0;
                mistakeCounterTextView.setText("--");
                isGameActive = false; // Set game state to inactive
                dialog.dismiss(); // Close the dialog
            });

            // Handle "Exit" button click
            btnExit.setOnClickListener(v -> {
                Intent i = new Intent(mistakeCounterTextView.getContext(), HomePage.class);
                mistakeCounterTextView.getContext().startActivity(i);// Exit the game
            });
            dialog.show();
        }
    }

    public static boolean isSudokuSolved() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                TextView textView = (TextView) sudokuBoard.getChildAt(i * GRID_SIZE + j);
                int currentValue = textView.getText().toString().isEmpty() ? 0 : Integer.parseInt(textView.getText().toString());
                int correctValue = originalGrid[i][j];
                if (currentValue != correctValue) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void showCongratulationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(sudokuBoard.getContext());
        builder.setTitle("Congratulations!");
        builder.setMessage("You've successfully solved the Sudoku puzzle!");
//        builder.setPositiveButton("OK", (dialog, which) ->
//                Intent i = new Intent();
//                dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
