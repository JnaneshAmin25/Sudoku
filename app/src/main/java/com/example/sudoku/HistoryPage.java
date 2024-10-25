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
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // Adjust based on your database structure
        currentUserId = "currentUserId"; // Replace with logic to get the current user's ID

        // Load match data
        loadCardViews();
    }

    private void openSortOptions() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SortFragment sortFragment = new SortFragment(this);
        sortFragment.show(fragmentManager, "sortFragment");
    }

    private void loadCardViews() {
        // Fetch match data from Firebase for the current user
        databaseReference.child(currentUserId).child("matches").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<MatchData> matchList = new ArrayList<>();

                if (dataSnapshot.exists()) {
                    // Iterate through each match data
                    for (DataSnapshot matchSnapshot : dataSnapshot.getChildren()) {
                        MatchData match = matchSnapshot.getValue(MatchData.class);
                        matchList.add(match);
                    }

                    // Log to verify the fetched matches
                    Log.d("HistoryPage", "Matches fetched: " + matchList.size());

                    // Display the matches in CardViews
                    for (MatchData match : matchList) {
                        addCardView(match.getTimer(), match.getScore(), match.getMistakes(), match.getCompletion());
                    }
                } else {
                    Log.d("HistoryPage", "No matches found for user.");
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors here (only one definition is needed)
                // You can log the error or display a message
            }
        });
    }

    private void addCardView(String timer, String score, String mistakes, String completion) {
        // Inflate the card_view.xml layout
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_view, cardContent, false);

        // Set the data into the CardView's TextViews
        TextView timerMode = cardView.findViewById(R.id.Timer_Mode);
        TextView scoreValue = cardView.findViewById(R.id.scoreValue);
        TextView mistakesValue = cardView.findViewById(R.id.mistakesValue);
        TextView completionText = cardView.findViewById(R.id.completionText);

        timerMode.setText(timer);
        scoreValue.setText(score);
        mistakesValue.setText(mistakes);
        completionText.setText(completion);

        // Set background color based on completion status
        if ("Completed".equals(completion)) {
            completionText.setBackgroundColor(Color.GREEN); // Set background to green
            completionText.setTextColor(Color.WHITE); // Set text color to white for contrast
        } else if ("Failed".equals(completion)) {
            completionText.setBackgroundColor(Color.RED); // Set background to red
            completionText.setTextColor(Color.WHITE); // Set text color to white for contrast
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
        private String score;
        private String mistakes;
        private String completion;

        public MatchData() {
            // Default constructor required for calls to DataSnapshot.getValue(MatchData.class)
        }

        public MatchData(String timer, String score, String mistakes, String completion) {
            this.timer = timer;
            this.score = score;
            this.mistakes = mistakes;
            this.completion = completion;
        }

        public String getTimer() {
            return timer;
        }

        public String getScore() {
            return score;
        }

        public String getMistakes() {
            return mistakes;
        }

        public String getCompletion() {
            return completion;
        }
    }
}
