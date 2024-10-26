package com.example.sudoku;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference; // Firebase Database reference
    private EditText signupEmailEditText, signupPasswordEditText, signupConfPasswordEditText, unameEditText;
    private RelativeLayout submitGroup;
    private String def;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        def="gs://sudoku-cdfa8.appspot.com/images/1729853804947.jpg";

        auth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        databaseReference = FirebaseDatabase.getInstance().getReference("Users"); // Initialize Firebase Database reference

        // Find views
        signupEmailEditText = view.findViewById(R.id.signupEmailEditText);
        signupPasswordEditText = view.findViewById(R.id.signupPasswordEditText);
        signupConfPasswordEditText = view.findViewById(R.id.signupConfPasswordEditText);
        unameEditText = view.findViewById(R.id.unameEditText);
        submitGroup = view.findViewById(R.id.submitGroup);

        // Focus listener for the username field
        unameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                unameEditText.setBackgroundResource(R.drawable.rounded_edittext); // Set focused border
            } else {
                unameEditText.setBackgroundResource(R.drawable.rounded_edittext); // Reset to default
            }
        });

        // Set the click listener for the sign-up button
        submitGroup.setOnClickListener(v -> {
            // Change the background to a drawable with rounded corners
            submitGroup.setBackgroundResource(R.drawable.selected_rounded_corner);

            // Call the handleSignup method with the EditTexts for email and password
            handleSignup(signupEmailEditText, signupPasswordEditText, signupConfPasswordEditText);

            // Reset the background to the original drawable after 2 seconds
            new android.os.Handler().postDelayed(() -> {
                submitGroup.setBackgroundResource(R.drawable.rounded_corner); // Replace with your original background drawable
            }, 70);
        });

        return view;
    }

    private void handleSignup(EditText emailEditText, EditText passwordEditText, EditText confPasswordEditText) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confPassword = confPasswordEditText.getText().toString().trim();
        String username = unameEditText.getText().toString().trim();

        // Validate email format
        if (!email.endsWith("@gmail.com")) {
            emailEditText.setError("Email must end with '@gmail.com'");
            return;
        }

        // Validate password criteria
        if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*[_@].*")) {
            passwordEditText.setError("Password must be at least 8 characters long, include uppercase, lowercase, and special characters _ or @");
            return;
        }

        // Validate matching passwords
        if (!password.equals(confPassword)) {
            confPasswordEditText.setError("Passwords do not match");
            return;
        }

        // Firebase authentication sign-up
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign-up success
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid(); // Get UID

                            // Save user info to Realtime Database
                            saveUserToDatabase(uid, email, username);

                            ToastUtils.showToast(getContext(), "Signup successful!", 2000); // 1 second duration

                            // Clear input fields after successful sign-up
                            unameEditText.setText("");
                            emailEditText.setText("");
                            passwordEditText.setText("");
                            confPasswordEditText.setText("");

                            // Load the LoginFragment after successful sign-up
                            loadLoginFragment();
                        }
                    } else {
                        // If sign-up fails, display a message to the user
                        ToastUtils.showToast(getContext(), "Signup failed: " + task.getException().getMessage(), 2000); // 1 second duration
                    }
                });
    }

    private void saveUserToDatabase(String uid, String email, String username) {
        // Create a user object
        Users user = new Users(uid, email, username);

        // Store user info under the user's UID in the database
        databaseReference.child(uid).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ToastUtils.showToast(getContext(), "User data saved to database.", 2000); // 1 second duration
                    } else {
                        ToastUtils.showToast(getContext(), "Failed to save user data.", 2000); // 1 second duration
                    }
                });
    }

    private void loadLoginFragment() {
        // Replace the current fragment with the LoginFragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Set up animations for fragment transition
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right,  // New fragment enters from right
                R.anim.fade_out,        // Current fragment fades out
                R.anim.fade_in,         // Current fragment fades in (when coming back)
                R.anim.slide_out_left   // New fragment slides out to the left (when coming back)
        );

        // Replace the current fragment with the LoginFragment
        fragmentTransaction.replace(R.id.fragment_container, new LoginFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
