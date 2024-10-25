package com.example.sudoku;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class HistoryPage extends AppCompatActivity implements SortFragment.SortOptionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_page);

        // Initialize views
        ImageView backButton = findViewById(R.id.backButton);
        ImageView sortIcon = findViewById(R.id.sortIcon);

        // Set back button listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Set sort icon listener
        sortIcon.setOnClickListener(v -> openSortOptions());
    }

    private void openSortOptions() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SortFragment sortFragment = new SortFragment(this);
        sortFragment.show(fragmentManager, "sortFragment");
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
}
