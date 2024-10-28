package com.example.sudoku;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.TransitionManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class Stat_page extends AppCompatActivity {

    private DonutProgressView donutView;
    private DonutProgressView donutView2;
    private TextView winsText;
    private TextView timeText;
    private ProgressBar progressBar;
    private TextView totalGamesText;
    private TextView avgCompletionTimeText;
    private TextView avgMistakesText;
    private TextView totalHoursText;
    private DatabaseReference databaseReference;
    private static boolean isActivityVisible; // Flag to prevent multiple refreshes
    boolean isDataRefreshed = false; // Flag to check if data is refreshed
    private NetworkChangeReceiver networkChangeReceiver; // Network receiver
    private View overlay;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_page);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser(); // Get the signed-in user

        // Get the user's unique ID from Firebase Authentication
        String userId = currentUser.getUid();

        // Initialize Firebase database reference for the specific user
        databaseReference = FirebaseDatabase.getInstance("https://sudoku-cdfa8-default-rtdb.firebaseio.com/")
                .getReference("GameScore").child(userId);

        // Initialize views
        donutView = findViewById(R.id.donut_view);
        donutView2 = findViewById(R.id.donut_view2);
        winsText = findViewById(R.id.wins_text);
        timeText = findViewById(R.id.time_text);
        totalGamesText = findViewById(R.id.totalgames);
        avgCompletionTimeText = findViewById(R.id.avgcompletion);
        avgMistakesText = findViewById(R.id.avgmistakes);
        totalHoursText = findViewById(R.id.totalhours);
        progressBar = findViewById(R.id.progressbar);
        overlay = findViewById(R.id.overlay);

        // Back Button (optional functionality)
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Set placeholder text to avoid layout shifts before data loads
        setPlaceholderText();

        // Register network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Check network availability and fetch data accordingly
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(Stat_page.this, "No Internet", Toast.LENGTH_SHORT).show();
            Intent in = new Intent(Stat_page.this, no_internet.class);
            startActivity(in);
            finish();
        } else {
            fetchDataFromFirebase(userId); // Fetch data for the signed-in user
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver); // Unregister to prevent leaks
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true; // Mark activity as visible
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false; // Mark activity as not visible
    }

    public static boolean isActivityVisible() {
        return isActivityVisible;
    }

    // Refresh data from Firebase
    public void refreshDataFromFirebase(String userId) {
        if (!isDataRefreshed) {
            fetchDataFromFirebase(userId); // Call your existing fetch method
            isDataRefreshed = true; // Set the flag to true to prevent looping
        }
    }

    private void fetchDataFromFirebase(String userId) {
        showLoading(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int totalWins = 0;
                    int totalGamesPlayed = 0;
                    long totalTimeLeft = 0;
                    int totalMistakes = 0;
                    long bestCompletionTime = Long.MAX_VALUE;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        totalGamesPlayed++;

                        // Retrieve completion status and isSolved values
                        String completionStatus = snapshot.child("completionStatus").getValue(String.class);
                        Boolean isSolved = snapshot.child("isSolved").getValue(Boolean.class);

                        if ("completed".equals(completionStatus) && Boolean.TRUE.equals(isSolved)) {
                            totalWins++;

                            // Retrieve timerMode and timeLeft with error handling for timeLeft
                            String timerMode = snapshot.child("timerMode").getValue(String.class);
                            Object timeLeftObj = snapshot.child("timeLeft").getValue();
                            Long timeLeft = null;

                            // Handle different types for timeLeft (Long or String)
                            if (timeLeftObj instanceof Long) {
                                timeLeft = (Long) timeLeftObj;
                            } else if (timeLeftObj instanceof String) {
                                try {
                                    timeLeft = Long.parseLong((String) timeLeftObj);
                                } catch (NumberFormatException e) {
                                    Log.e("StatPage", "Error parsing timeLeft as Long: " + e.getMessage());
                                }
                            }

                            long totalGameTimeMillis = convertTimerModeToMillis(timerMode);

                            if (timeLeft != null && totalGameTimeMillis > 0) {
                                long elapsedTime = totalGameTimeMillis - timeLeft;
                                totalTimeLeft += elapsedTime;
                                bestCompletionTime = Math.min(bestCompletionTime, elapsedTime);
                            }
                        }

                        // Retrieve and handle mistakesCount similarly
                        Object mistakesObj = snapshot.child("mistakesCount").getValue();
                        Integer mistakesCount = 0;

                        if (mistakesObj instanceof Long) {
                            mistakesCount = ((Long) mistakesObj).intValue();
                        } else if (mistakesObj instanceof String) {
                            try {
                                mistakesCount = Integer.parseInt((String) mistakesObj);
                            } catch (NumberFormatException e) {
                                Log.e("StatPage", "Error parsing mistakesCount as Integer: " + e.getMessage());
                            }
                        }
                        totalMistakes += mistakesCount;
                    }

                    if (totalWins == 0) {
                        bestCompletionTime = 0;
                    }

                    float averageCompletionTime = totalWins > 0 ? (float) totalTimeLeft / totalWins : 0;

                    int averageMistakes = totalGamesPlayed > 0 ? Math.round((float) totalMistakes / totalGamesPlayed) : 0;

                    float totalHoursPlayed = totalTimeLeft / 1000f / 60f / 60f;

                    int finalTotalWins = totalWins;
                    long finalBestCompletionTime = bestCompletionTime;
                    int finalTotalGamesPlayed = totalGamesPlayed;
                    runOnUiThread(() -> {
                        winsText.setText(String.valueOf(finalTotalWins));
                        avgMistakesText.setText("Average Mistakes: " + averageMistakes);
                        updateUI(finalTotalWins, finalBestCompletionTime, finalTotalGamesPlayed, averageCompletionTime, averageMistakes, totalHoursPlayed);
                    });
                } else {
                    redirectToNoDataPage();
                    Log.e("StatPage", "Data does not exist!");
                }
                showLoading(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("StatPage", "Database error: " + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }





    /**
     * Convert timer mode to milliseconds.
     */
    private long convertTimerModeToMillis(String timerMode) {
        switch (timerMode) {
            case "5 sec":
                return 5 * 1000;
            case "5 Min":
                return 5 * 60 * 1000;
            case "10 Min":
                return 10 * 60 * 1000;
            case "15 Min":
                return 15 * 60 * 1000;
            default:
                return 0;
        }
    }

    /**
     * Convert date and time string to milliseconds.
     */
    private long convertDateTimeToMillis(String dateTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(dateTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e("StatPage", "Error parsing date: " + e.getMessage());
            return 0;
        }
    }



    private void updateUI(int totalWins, long bestCompletionTime, int totalGamesPlayed, float avgCompletionTime, int avgMistakes, float totalHoursPlayed) {
        // Reset UI fields before setting new values
        totalGamesText.setText("0");
        avgCompletionTimeText.setText("0 min");
        avgMistakesText.setText("0");
        totalHoursText.setText("0.0 hrs");

        // Calculate minutes and seconds for bestCompletionTime
        long minutes = bestCompletionTime / (1000 * 60);
        long seconds = (bestCompletionTime / 1000) % 60;

        // Display best time in "MM:SS" format
        if (bestCompletionTime < 60000) {  // If less than a minute, show only seconds
            winsText.setText(String.format(Locale.getDefault(), "%d sec", bestCompletionTime / 1000));
        } else {  // Otherwise, show both minutes and seconds
            winsText.setText(String.format(Locale.getDefault(), "%02d:%02d min", minutes, seconds));
        }

        // Calculate and display avgCompletionTime in "MM:SS" format
        long avgMinutes = (long) (avgCompletionTime / 1000 / 60);
        long avgSeconds = (long) ((avgCompletionTime / 1000) % 60);
        avgCompletionTimeText.setText(String.format(Locale.getDefault(), "Average Completion Time: %02d:%02d min", avgMinutes, avgSeconds));

        // Set other UI elements
        timeText.setText(String.valueOf(totalWins));
        totalGamesText.setText("Total Games Played: " + totalGamesPlayed);
        avgMistakesText.setText("Average Mistakes: " + avgMistakes);
        Log.d("StatPage", "Mistakes for this game: " + avgMistakes);

        totalHoursText.setText(String.format(Locale.getDefault(), "Total Hours Played: %.1f hrs", totalHoursPlayed));

        // Update donut charts with normalized values
        float maxWinsCap = Math.max(10, totalWins);
        float maxTimeCap = Math.max(bestCompletionTime, 5 * 60 * 1000);

        float totalWinsNormalized = totalWins / maxWinsCap;
        float bestTimeNormalized = bestCompletionTime / maxTimeCap;

        DonutSection winsSection = new DonutSection("wins_section", Color.parseColor("#3D52A0"), totalWinsNormalized);
        DonutSection timeSection = new DonutSection("time_section", Color.parseColor("#3D52A0"), bestTimeNormalized);

        TransitionManager.beginDelayedTransition(findViewById(android.R.id.content));
        donutView.setCap(totalWinsNormalized > 1 ? totalWinsNormalized : 1f);
        donutView2.setCap(bestTimeNormalized > 1 ? bestTimeNormalized : 1f);
        donutView.submitData(Arrays.asList(winsSection));
        donutView2.submitData(Arrays.asList(timeSection));
    }








    private void setPlaceholderText() {
        winsText.setText("0");
        timeText.setText("0.0");
        totalGamesText.setText("Loading...");
        avgCompletionTimeText.setText("Loading...");
        avgMistakesText.setText("Loading...");
        totalHoursText.setText("Loading...");
    }

    public void resetDataRefreshFlag() {
        isDataRefreshed = false;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE); // Show the overlay
        } else {
            progressBar.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE); // Hide the overlay
        }
    }

    private void redirectToNoDataPage() {
        Intent intent = new Intent(Stat_page.this, Nodatafound.class);
        startActivity(intent);
        finish();  // Finish the current activity so that the user cannot go back to it
    }

    public String getUserId() {
        return currentUser.getUid();
    }

}
