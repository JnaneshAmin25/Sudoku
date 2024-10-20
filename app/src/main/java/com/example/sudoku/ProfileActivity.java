package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton homeIcon, editProfileButton;
    private RelativeLayout historyButton, statisticsButton, logoutButton;
    private TextView userName, userEmail, userDOB, userGender;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseApp.initializeApp(this);

        // Initialize Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        homeIcon = findViewById(R.id.homeIcon);
        editProfileButton = findViewById(R.id.editProfileButton);
        historyButton = findViewById(R.id.historyButton);
        statisticsButton = findViewById(R.id.statisticsButton);
        logoutButton = findViewById(R.id.logoutButton);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userDOB = findViewById(R.id.userDOB);
        userGender = findViewById(R.id.userGender);

        // Check if user is logged in
        if (currentUser != null) {
            String userUID = currentUser.getUid(); // Get UID of the logged-in user
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userUID);

            Log.d("ProfileActivity", "User UID: " + userUID);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String dob = snapshot.child("dob").getValue(String.class); // Assuming this is the field name in your database
                        String gender = snapshot.child("gender").getValue(String.class); // Assuming this is the field name in your database

                        Log.d("ProfileActivity", "Username: " + username);
                        Log.d("ProfileActivity", "Email: " + email);
                        Log.d("ProfileActivity", "DOB: " + dob);
                        Log.d("ProfileActivity", "Gender: " + gender);

                        userName.setText(username);
                        userEmail.setText(email);
                        userDOB.setText(dob != null ? dob : "--"); // Handle potential null value
                        userGender.setText(gender != null ? gender : "--"); // Handle potential null value
                    } else {
                        ToastUtils.showToast(ProfileActivity.this, "User data not found!", 2000);
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastUtils.showToast(ProfileActivity.this, "Failed to load user data.", 2000);
                }
            });
        } else {
            ToastUtils.showToast(this, "User not logged in!", 2000);
            // Optionally redirect to login or another activity
            startActivity(new Intent(ProfileActivity.this, LoginFragment.class)); // Assuming you have a LoginActivity
            finish();
        }

        homeIcon.setOnClickListener(v -> {
            // Navigate back to the home page and close the current activity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close ProfileActivity
        });

        editProfileButton.setOnClickListener(v -> {
            ToastUtils.showToast(ProfileActivity.this, "Edit Profile Clicked", 2000);
        });

        historyButton.setOnClickListener(v -> {
            ToastUtils.showToast(ProfileActivity.this, "History Clicked", 2000);
        });

        statisticsButton.setOnClickListener(v -> {
            ToastUtils.showToast(ProfileActivity.this, "Statistics Clicked", 2000);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            ToastUtils.showToast(ProfileActivity.this, "Logged out successfully.", 2000);
            startActivity(new Intent(ProfileActivity.this, MainActivity.class)); // Redirect to login activity
            finish(); // Close ProfileActivity
        });
    }
}