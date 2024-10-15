package com.example.sudoku;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
    private RelativeLayout submitGroup;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.signinEmailEditText);
        passwordEditText = view.findViewById(R.id.signinPasswordEditText);
        forgotPassTxt = view.findViewById(R.id.forgotPassTxt);
        submitGroup = view.findViewById(R.id.submitGroup);

        final boolean[] isPasswordVisible = {false};

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Handle the Sign-In button click
        view.findViewById(R.id.submitGroup).setOnClickListener(v -> {
            // Change the background to a drawable with rounded corners
            v.setBackgroundResource(R.drawable.selected_rounded_corner);

            // Call the signInUser method to handle sign-in
            signInUser();

            // Reset the background to the original drawable after 2 seconds
            new android.os.Handler().postDelayed(() -> {
                v.setBackgroundResource(R.drawable.rounded_corner); // Replace with the original background drawable
            }, 70);
        });

        // Handle the "Forgot Password" click
        forgotPassTxt.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            // Change text color to selectedButtonColor
            forgotPassTxt.setTextColor(getResources().getColor(R.color.selectedButtonColor));

            // Create a handler to reset the color after 70 milliseconds
            new Handler().postDelayed(() -> {
                // Reset the text color to original
                forgotPassTxt.setTextColor(getResources().getColor(R.color.grey));
            }, 70); // Delay of 70 milliseconds

            // Check if email is empty
            if (email.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordResetEmail(email);
            }
        });


        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // Right drawable position
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if the touch is on the drawableEnd (show/hide icon)
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    // Toggle password visibility
                    if (isPasswordVisible[0]) {
                        // Hide password
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.show_password, 0); // Set "show" icon
                    } else {
                        // Show password
                        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.hide_password, 0); // Set "hide" icon
                    }

                    // Maintain font style, color, and size
                    passwordEditText.setTypeface(getResources().getFont(R.font.poppinsregular));
                    passwordEditText.setTextColor(getResources().getColor(R.color.grey));
                    passwordEditText.setTextSize(12);

                    isPasswordVisible[0] = !isPasswordVisible[0];

                    // Set cursor to the end of the text
                    passwordEditText.setSelection(passwordEditText.getText().length());

                    return true; // Consume the touch event
                }
            }
            return false;
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

    private void sendPasswordResetEmail(String email) {
        // Firebase method to send a password reset email
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to send password reset email. Try again later.", Toast.LENGTH_SHORT).show();
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
            // Create an intent to navigate to Samplepage activity
            Intent intent = new Intent(getActivity(), Samplepage.class);
            startActivity(intent);

            //Close the current activity if you don't want the user to come back to the login screen
            if (getActivity() != null) {
                getActivity().finish();
            }
        } else {
            // User is signed out
            Toast.makeText(getActivity(), "Please sign in.", Toast.LENGTH_SHORT).show();
        }
    }
}



