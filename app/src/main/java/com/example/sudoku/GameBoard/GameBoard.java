package com.example.sudoku.GameBoard;

import static com.example.sudoku.GameBoard.GameData.originalGrid;
import static com.example.sudoku.GameBoard.SudokuGenerator.GRID_SIZE;
import static com.example.sudoku.GameBoard.SudokuGenerator.generateSudoku;
import static com.example.sudoku.GameBoard.SudokuGenerator.grid;
import static com.example.sudoku.GameBoard.SudokuGenerator.setupNumberButtons;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

public class GameBoard extends AppCompatActivity implements View.OnClickListener {

    public static TextView mistakeCounterTextView; // TextView for displaying mistakes
    public static int mistakes = 0; // Count of mistakes made
    private static final int MAX_MISTAKES = 3; // Maximum allowed mistakes
    static MaterialButton[]  numberButtons;
    public  static ImageView timerimg;
    private ImageView restartimg;
    private ImageView erasevalue;
    private ImageView backtohome;
    private static TextView timerTextView;
    private CountDownTimer countDownTimer;
    private static boolean isTimerRunning = false;
    private static long timeLeftInMillis = 600000; // Default: 10 minutes
    public static GridLayout sudokuBoard;
    public static boolean isGameActive = true;
    public static Stack<int[][]> moveStack;
    public boolean wasTimerRunning;


    // Game statistics variables
    public static int fb_undoCount = 0;
    public static int fb_pauseCount = 0;
    public static int fb_eraseCount = 0;
    public static int fb_mistakesCount = 0;
    public static long fb_startTime = 0;
    public static boolean fb_gameAbandoned = false;
    public static boolean fb_isSolved = false;
    public static String gameId;
    public static String fb_timerMode = "no_time"; // Track the timer mode
    public static String completionStatus = "in_progress"; // "completed", "failed", or "abandoned"
    public static String fb_reasonForFailure = "";
    private static Context context;

    // Firebase setup
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    public static DatabaseReference gameScoreRef;
    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseUser currentUser;

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
        context = getApplicationContext();

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            initFirebase();
        }


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
                fb_pauseCount++;
                gameScoreRef.child(gameId).child("pauseCount").setValue(fb_pauseCount);
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


//        String time = getIntent().getStringExtra("time");
//        startNewGame(time);
//        startTimer();

        startNewGame("--");

        timerimg.setEnabled(false);

    }

    // Initialize Firebase Database Reference
    private static void initFirebase() {
        String uid = currentUser.getUid();
        gameScoreRef = firebaseDatabase.getReference("GameScore").child(uid);
    }


    // Start a new game
    public static void startNewGame(String timerMode) {
        gameId = gameScoreRef.push().getKey();

        fb_startTime = System.currentTimeMillis();
        fb_undoCount = 0;
        fb_pauseCount = 0;
        fb_eraseCount = 0;
        fb_mistakesCount = 0;
        mistakes = 0;
        mistakeCounterTextView.setText("0/3");
        fb_gameAbandoned = false;
        GameBoard.fb_isSolved = false;
        GameBoard.fb_timerMode = timerMode;

        Map<String, Object> gameData = new HashMap<>();
        //gameData.put("gameId", gameId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        gameData.put("undoCount",fb_undoCount);
        gameData.put("pauseCount",fb_pauseCount);
        gameData.put("eraseCount",fb_eraseCount);
        gameData.put("mistakesCount",fb_mistakesCount);
        gameData.put("dateStarted", dateFormat.format(new Date(fb_startTime)));
        gameData.put("timeStarted", timeFormat.format(new Date(fb_startTime)));
        gameData.put("timerMode", timerMode);
        gameData.put("completionStatus", completionStatus);
        gameScoreRef.child(gameId).setValue(gameData);
    }



    public static void endGame(Context context, boolean isSolved, String failureReason) {
        long timeLeft = calculateTimeLeft(timeLeftInMillis, isTimerRunning);
        fb_gameAbandoned = false;
        GameBoard.isGameActive = false;
        GameBoard.fb_isSolved = isSolved;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeEnded = timeFormat.format(new Date(System.currentTimeMillis()));
        if(isSolved){
            completionStatus = "completed";
        }else{
            completionStatus = "failed";
        }

        long score = calculateScore(completionStatus, fb_timerMode,timeLeft , fb_eraseCount,
                fb_mistakesCount, fb_pauseCount, fb_undoCount);

        Map<String, Object> endGameData = new HashMap<>();
        endGameData.put("isSolved", isSolved);
        endGameData.put("completionStatus", completionStatus);
        endGameData.put("timeEnded", timeEnded);
        endGameData.put("timeLeft",timeLeft+" Min");
        endGameData.put("Total Score",score);

//        if (!isSolved) {
//            fb_reasonForFailure = failureReason; // Assuming you have a class-level variable for this
//            endGameData.put("reasonForFailure", fb_reasonForFailure);
//        }

        gameScoreRef.child(gameId).updateChildren(endGameData).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Log.e("success", "Game saved");
            } else {
                Log.e("error", "Error saving game data: " + task.getException());
            }
        });

    }
    public static long calculateTimeLeft(long timeLeftInMillis, boolean isTimerRunning) {
        if (isTimerRunning) {
            return timeLeftInMillis/60000;
        } else {
            return 0;
        }
    }

    public static long calculateScore(String completionStatus,String timerMode, long timeLeft,
                                      int eraseCount, int mistakeCount, int pauseCount,
                                       int undoCount) {
        long score = 0;

        // Scoring based on completion status
        switch (completionStatus) {
            case "completed":
                score = 1000;
                break;
            case "failed":
                score = 500;
                break;
            case "abandoned":
                score = 250;
                break;
        }

        // Penalize for longer durations (e.g., -1 point for every 10 seconds over 10 seconds)
        if (timeLeft > 10) {
            score -= (timeLeft - 1); // Penalize 1 point for each additional second
        }

        // Penalties for erases, mistakes, pauses, and undos
        score -= (eraseCount * 5L);
        score -= (mistakeCount * 10L);
        score -= (pauseCount * 2L);
        score -= (undoCount * 3L);

        // Bonus for using timer mode
        if (!timerMode.equals("No Time")) {
            score += 10; // Add bonus points for timer mode
        }

        return score;
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < 9; i++) {
            if (v.getId() == numberButtons[i].getId()) {
                int number = i + 1;
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
            GameBoard.abandonGame();
            Intent i = new Intent(dialog.getContext(), HomePage.class);
            dialog.getContext().startActivity(i);
            finish();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void abandonGame() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        GameScore gameScore = new GameScore();
        completionStatus = "Abandoned";

        gameScore.setGameId(gameId);
        gameScore.setDateTime( new SimpleDateFormat("yyyy-MM-dd").format(new Date(fb_startTime)));
        gameScore.setMistakesCount(fb_mistakesCount); // Mistakes made in the game
        gameScore.setSolved(false);
        gameScore.setTimerMode("--");
        gameScore.setUndoCount(fb_undoCount);
        gameScore.setPauseCount(fb_pauseCount);
        gameScore.setEraseCount(fb_eraseCount);
        gameScore.setCompletionStatus(completionStatus); // Indicating the game was abandoned
        long timeLeft = calculateTimeLeft(timeLeftInMillis, isTimerRunning);
        long score = calculateScore(completionStatus, fb_timerMode,timeLeft , fb_eraseCount,
                fb_mistakesCount, fb_pauseCount, fb_undoCount);
        gameScore.setScore(score);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("GameScore").child(uid).child(gameScore.getGameId());
        database.setValue(gameScore).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("GameBoard", "Game abandoned successfully.");
            } else {
                Log.e("GameBoard", "Failed to abandon game: " + task.getException());
            }
        });
    }



    private void showTimeSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_timer_selection);

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
        Button setTimeBtn = dialog.findViewById(R.id.btn_set_time);

        if (isGameActive) {
            abandonGame();
        }

        wasTimerRunning = isTimerRunning;

        setTimeBtn.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.radio_5sec) {
                timeLeftInMillis = 5000; // 5 minutes
                timerTextView.setText("00:05");
                timerimg.setEnabled(true);
                startNewGame("5 sec");
                generateSudoku(sudokuBoard, getApplicationContext());
                startTimer();
            }
            else if (selectedId == R.id.radio_5min) {
                timeLeftInMillis = 300000; // 5 minutes
                timerTextView.setText("05:00");
                timerimg.setEnabled(true);
                startNewGame("5 Min");
                generateSudoku(sudokuBoard, getApplicationContext());
                startTimer();
            } else if (selectedId == R.id.radio_10min) {
                timeLeftInMillis = 600000; // 10 minutes
                timerTextView.setText("10:00");
                startNewGame("10 Min");
                generateSudoku(sudokuBoard, getApplicationContext());
                timerimg.setEnabled(true);
                startTimer();
            } else if (selectedId == R.id.radio_15min) {
                timeLeftInMillis = 900000; // 15 minutes
                timerTextView.setText("15:00");
                startNewGame("15 Min");
                generateSudoku(sudokuBoard, getApplicationContext());
                timerimg.setEnabled(true);
                startTimer();
            } else if (selectedId == R.id.radio_no_time) {
                pauseTimer();
                timerTextView.setText("--");
                startNewGame("No Time");
                generateSudoku(sudokuBoard, getApplicationContext());
                timerimg.setEnabled(false);
            }

            if (wasTimerRunning) {
                // Mark the game as abandoned
                GameBoard.abandonGame(); // Call the method to handle game abandonment
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

        fb_undoCount++;
        gameScoreRef.child(gameId).child("undoCount").setValue(fb_undoCount);

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

    public void startTimer() {
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
                GameBoard.endGame(getApplicationContext(),false, "Time Up");
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
        isTimerRunning = true;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                // Timer finished, handle game over
                isTimerRunning = false;
                endGame(context, false, "Time's up!");
            }
        }.start();
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
        mistakeCounterTextView.setText(String.format("%d/%d", mistakes, MAX_MISTAKES));
        // Update the display
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
                abandonGame();
                Intent i = new Intent(context, HomePage.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                //mistakeCounterTextView.getContext().startActivity(i);// Exit the game
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

// Set the "OK" button and handle the click event
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Create an Intent to go back to the HomePage
            Intent i = new Intent(sudokuBoard.getContext(), HomePage.class);
            // Start the HomePage activity
            sudokuBoard.getContext().startActivity(i);
        });

// Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }


}
