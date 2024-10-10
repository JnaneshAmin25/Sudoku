package com.example.sudoku;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView forgotPassTxt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.signinEmailEditText);
        passwordEditText = view.findViewById(R.id.signinPasswordEditText);
        forgotPassTxt = view.findViewById(R.id.forgotPassTxt);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Handle the Sign-In button click
        view.findViewById(R.id.signinButton).setOnClickListener(v -> signInUser());

        forgotPassTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change the font color when clicked
                forgotPassTxt.setTextColor(getResources().getColor(R.color.selectedButtonColor));  // Change R.color.new_color to the desired color
            }
        });

        return view;
    }

    private void signInUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show custom progress toast
        Toast customToast = createCustomProgressToast();
        customToast.show();

        // Sign in with Firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Dismiss the custom toast
                    customToast.cancel();

                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = auth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Toast createCustomProgressToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.progress_toast, (ViewGroup) getView().findViewById(R.id.customToastContainer));

        // Create a new Toast
        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);  // Center the toast on the screen
        return toast;
    }

    private void updateUI(FirebaseUser user) {
        // Update UI based on Firebase user login status
        if (user != null) {
            // User is signed in
            Toast.makeText(getActivity(), "Login Successful!", Toast.LENGTH_SHORT).show();
        } else {
            // User is signed out
            Toast.makeText(getActivity(), "Please sign in.", Toast.LENGTH_SHORT).show();
        }
    }
}
