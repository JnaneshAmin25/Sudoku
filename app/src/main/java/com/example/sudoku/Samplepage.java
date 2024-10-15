package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Samplepage extends AppCompatActivity {

    private Button logoutButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samplepage);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Find the logout button
        logoutButton = findViewById(R.id.Logout);

        // Set the logout button listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log out the user
                auth.signOut();

                // Finish the SamplePage activity
                finish();

                // Navigate to MainActivity and load LoginFragment
                Intent intent = new Intent(Samplepage.this, MainActivity.class);
                intent.putExtra("loadLoginFragment", true);  // Extra to load LoginFragment
                startActivity(intent);
            }
        });
    }
}
