package com.example.sudoku;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class HistoryPage extends AppCompatActivity implements SortFragment.SortOptionListener {

    private LinearLayout cardContent;
    private DatabaseReference databaseReference;
    private String currentUserId;
    private ArrayList<MatchData> matchList = new ArrayList<>();
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_page);

        // Initialize views
        searchBar = findViewById(R.id.searchBar);
        ImageView searchIcon = findViewById(R.id.search_icon);
        ImageView refreshButton = findViewById(R.id.refreshButton); // Refresh button
        ImageView backButton = findViewById(R.id.backButton);
        ImageView sortIcon = findViewById(R.id.sortIcon);
        cardContent = findViewById(R.id.cardContent);

        // Set back button listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Set sort icon listener
        sortIcon.setOnClickListener(v -> openSortOptions());

        // Set click listener on the search icon ImageView
        searchIcon.setOnClickListener(v -> filterCardsBySearch());

        // Set click listener for refresh button
        refreshButton.setOnClickListener(v -> sortAndDisplayRecentGames());

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("GameScore");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
                matchList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        String timerMode = gameSnapshot.child("timerMode").getValue(String.class);
                        Long score = gameSnapshot.child("Total Score").getValue(Long.class);
                        Long mistakesCount = gameSnapshot.child("mistakesCount").getValue(Long.class);
                        String completionStatus = gameSnapshot.child("completionStatus").getValue(String.class);
                        String dateStarted = gameSnapshot.child("dateStarted").getValue(String.class);

                        // Only add games with completionStatus as "completed" or "failed"
                        if ("completed".equals(completionStatus) || "failed".equals(completionStatus)) {
                            MatchData match = new MatchData(
                                    timerMode,
                                    score,
                                    mistakesCount != null ? mistakesCount.intValue() : 0,
                                    completionStatus,
                                    dateStarted
                            );
                            matchList.add(match);
                        }
                    }
                    displayCards();
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

    private void displayCards() {
        cardContent.removeAllViews();

        for (MatchData match : matchList) {
            String scoreString = (match.getScore() != null) ? String.valueOf(match.getScore()) : "0";
            String mistakesString = String.valueOf(match.getMistakes());

            addCardView(match.getTimer(), scoreString, mistakesString, match.getCompletion());
        }
    }

    private void addCardView(String timer, String score, String mistakes, String completion) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_view, cardContent, false);

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

        cardContent.addView(cardView);
    }

    private void filterCardsBySearch() {
        String query = searchBar.getText().toString().trim().toLowerCase();

        if (query.isEmpty()) {
            displayCards(); // Show all cards if search query is empty
            return;
        }

        // Filter the matchList based on query
        ArrayList<MatchData> filteredList = new ArrayList<>();
        for (MatchData match : matchList) {
            if ((match.getTimer() != null && match.getTimer().toLowerCase().contains(query)) ||
                    (match.getScore() != null && String.valueOf(match.getScore()).contains(query)) ||
                    (match.getCompletion() != null && match.getCompletion().toLowerCase().contains(query))) {
                filteredList.add(match);
            }
        }

        // Update the display with the filtered list
        displayFilteredCards(filteredList);
    }

    private void displayFilteredCards(ArrayList<MatchData> filteredList) {
        cardContent.removeAllViews(); // Clear existing views

        for (MatchData match : filteredList) {
            String scoreString = (match.getScore() != null) ? String.valueOf(match.getScore()) : "0";
            String mistakesString = String.valueOf(match.getMistakes());

            addCardView(match.getTimer(), scoreString, mistakesString, match.getCompletion());
        }
    }

    private void sortAndDisplayRecentGames() {
        // Sort matchList by dateStarted in descending order (most recent first)
        Collections.sort(matchList, (a, b) -> b.getDateStarted().compareTo(a.getDateStarted()));
        displayCards(); // Display the sorted list
    }

    @Override
    public void onSortOptionSelected(String option) {
        switch (option) {
            case "Recent":
                sortAndDisplayRecentGames();
                break;
            case "Highest Score":
                Collections.sort(matchList, (a, b) -> Long.compare(b.getScore() != null ? b.getScore() : 0, a.getScore() != null ? a.getScore() : 0));
                displayCards();
                break;
            case "Lowest Score":
                Collections.sort(matchList, (a, b) -> Long.compare(a.getScore() != null ? a.getScore() : 0, b.getScore() != null ? b.getScore() : 0));
                displayCards();
                break;
        }
    }

    public static class MatchData {
        private String timer;
        private Long score;
        private int mistakes;
        private String completion;
        private String dateStarted;

        public MatchData(String timer, Long score, int mistakes, String completion, String dateStarted) {
            this.timer = timer;
            this.score = score;
            this.mistakes = mistakes;
            this.completion = completion;
            this.dateStarted = dateStarted;
        }

        public String getTimer() { return timer; }
        public Long getScore() { return score; }
        public int getMistakes() { return mistakes; }
        public String getCompletion() { return completion; }
        public String getDateStarted() { return dateStarted; }
    }
}
