package com.example.sudoku;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.cardview.widget.CardView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class HistoryPage extends AppCompatActivity implements SortFragment.SortOptionListener {

    private LinearLayout cardContent;
    private DatabaseReference databaseReference; // Reference to your database
    private String currentUserId; // To store current user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_page);
        // Initialize views
        ImageView backButton = findViewById(R.id.backButton);
        ImageView sortIcon = findViewById(R.id.sortIcon);
        cardContent = findViewById(R.id.cardContent);

        // Set back button listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Set sort icon listener
        sortIcon.setOnClickListener(v -> openSortOptions());

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("GameScore"); // Adjust based on your database structure
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Replace with logic to get the current user's ID

        // Load match data
        loadCardViews();
    }

    private void openSortOptions() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SortFragment sortFragment = new SortFragment(this);
        sortFragment.show(fragmentManager, "sortFragment");
    }

    private void loadCardViews() {
        // Fetch game data from Firebase for the current user
        databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<MatchData> matchList = new ArrayList<>();

                if (dataSnapshot.exists()) {
                    // Iterate through each game entry
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        String timerMode = gameSnapshot.child("timerMode").getValue(String.class);
                        Long score = gameSnapshot.child("Total Score").getValue(Long.class); // Retrieve as Long
                        Long mistakesCount = gameSnapshot.child("mistakesCount").getValue(Long.class); // Retrieve as Long
                        String completionStatus = gameSnapshot.child("completionStatus").getValue(String.class);

                        // Only add games with completionStatus as "completed" or "failure"
                        if ("completed".equals(completionStatus) || "failed".equals(completionStatus)) {
                            MatchData match = new MatchData(
                                    timerMode,
                                    score,
                                    mistakesCount != null ? mistakesCount.intValue() : 0,
                                    completionStatus
                            );
                            matchList.add(match);
                        }
                    }

                    // Display the filtered games in CardViews
                    for (MatchData match : matchList) {
                        // Convert score and mistakes to Strings here
                        String scoreString = (match.getScore() != null) ? String.valueOf(match.getScore()) : "0";
                        String mistakesString = String.valueOf(match.getMistakes());

                        addCardView(match.getTimer(), scoreString, mistakesString, match.getCompletion());
                    }
                } else {
                    Log.d("HistoryPage", "No games found for user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HistoryPage", "Database error: " + databaseError.getMessage());
            }
        });
    }





    private void addCardView(String timer, String score, String mistakes, String completion) {
        // Inflate the card_view.xml layout
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_view, cardContent, false);

        // Set the data into the CardView's TextViews
        TextView timerMode = cardView.findViewById(R.id.timer_value);
        TextView scoreValue = cardView.findViewById(R.id.scoreValue);
        TextView mistakesValue = cardView.findViewById(R.id.mistakesValue);
        TextView completionText = cardView.findViewById(R.id.completionText);

        timerMode.setText(timer);
        scoreValue.setText(score);
        mistakesValue.setText(mistakes);
        completionText.setText(completion);

        // Set background color based on completion status
        if ("completed".equals(completion)) {
            completionText.setBackgroundColor(Color.GREEN);
            completionText.setTextColor(Color.BLACK);
        } else if ("failed".equals(completion)) {
            completionText.setBackgroundColor(Color.RED);
            completionText.setTextColor(Color.BLACK);
        }

        // Add the CardView to the LinearLayout
        cardContent.addView(cardView);
    }

    // Handle the selected sort option
    @Override
    public void onSortOptionSelected(String option) {
        // Implement sorting logic based on selected option
        switch (option) {
            case "Recent":
                // Logic for sorting by recent
                break;
            case "Highest Score":
                // Logic for sorting by highest score
                break;
            case "Lowest Score":
                // Logic for sorting by lowest score
                break;
        }
    }

    // Create a model class for match data
    public static class MatchData {
        private String timer;
        private Long score;
        private int mistakes;
        private String completion;

        public MatchData() {
            // Default constructor required for calls to DataSnapshot.getValue(MatchData.class)
        }

        public MatchData(String timer, Long score, int mistakes, String completion) {
            this.timer = timer;
            this.score = score;
            this.mistakes = mistakes;
            this.completion = completion;
        }

        public String getTimer() {
            return timer;
        }

        public Long getScore() {
            return score;
        }

        public int getMistakes() {
            return mistakes;
        }

        public String getCompletion() {
            return completion;
        }
    }
}
