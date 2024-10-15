package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    Button signUpButton;
    Button signInButton;
    TextView welcomeText;
    RelativeLayout googleLogoGroup;
    RelativeLayout facebookLogoGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Add this to your strings.xml from Firebase console
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Load Sign-In fragment by default
        loadFragment(new LoginFragment(), false);

        signUpButton = findViewById(R.id.signUpButton);
        signInButton = findViewById(R.id.signInButton);
        googleLogoGroup = findViewById(R.id.googleLogoGroup);// Find the Google sign-in button
        facebookLogoGroup = findViewById(R.id.facebookLogoGroup);
        welcomeText = findViewById(R.id.welcome_bac);

        // Set default color for the buttons
        signUpButton.setBackgroundColor(getResources().getColor(R.color.selectedButtonColor));
        signInButton.setBackgroundColor(getResources().getColor(R.color.deselectedButtonColor));
        signUpButton.setEnabled(false);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SignUpFragment(), true);
                updateButtonState(true);
                welcomeText.setText(R.string.create_acc);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new LoginFragment(), false);
                updateButtonState(false);
                welcomeText.setText(R.string.welcome_bac);
            }
        });

        // Google Sign-In button click
        googleLogoGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogoGroup.setBackgroundResource(R.drawable.selected_rounded_corner);
                signInWithGoogle();
                new android.os.Handler().postDelayed(() -> {
                    googleLogoGroup.setBackgroundResource(R.drawable.rounded_corner); // Replace with your original background drawable
                }, 70);
            }
        });



        //facebook sign-In button click
        facebookLogoGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogoGroup.setBackgroundResource(R.drawable.selected_rounded_corner);

                new android.os.Handler().postDelayed(() -> {
                    facebookLogoGroup.setBackgroundResource(R.drawable.rounded_corner); // Replace with your original background drawable
                }, 70);
            }
        });
    }

    // Method to initiate Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Handle the result of the Google Sign-In
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign-In failed
                Log.w("GoogleSignIn", "Google sign in failed", e);
                Toast.makeText(MainActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Authenticate with Firebase using the Google account
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Google sign in successful", Toast.LENGTH_SHORT).show();
                            updateUI(user);
                            Intent intent = new Intent(MainActivity.this, Samplepage.class);
                            startActivity(intent);
                            MainActivity.this.finish();
                        } else {
                            // Sign in failed
                            Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and update UI
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, proceed to the next activity (SamplePage.java)
            Toast.makeText(MainActivity.this, "Logged in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();

            // Create an intent to navigate to SamplePage activity
            Intent intent = new Intent(MainActivity.this, Samplepage.class);
            startActivity(intent);
            MainActivity.this.finish(); // Optional: Finish the current activity so the user can't go back
        } else {
            // User is not signed in, load the SignUpFragment
            Toast.makeText(MainActivity.this, "Not logged in", Toast.LENGTH_SHORT).show();

            // Load the SignUpFragment
            FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Optionally, you can add custom animations for fragment transition
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,  // New fragment enters from right
                    R.anim.fade_out,        // Current fragment fades out
                    R.anim.fade_in,         // Current fragment fades in (when coming back)
                    R.anim.slide_out_left   // New fragment slides out to the left (when coming back)
            );

            fragmentTransaction.replace(R.id.fragment_container, new SignUpFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private void loadFragment(Fragment fragment, boolean isSignUp) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Clear the back stack (removes all previously added fragments)
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // Begin the transaction and replace the fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);

        // Commit the transaction
        fragmentTransaction.commit();
    }


    private void updateButtonState(boolean isSignUpSelected) {
        if (isSignUpSelected) {
            // Sign-Up selected, disable Sign-Up button, enable Sign-In button
            signUpButton.setEnabled(false);
            signUpButton.setBackgroundColor(getResources().getColor(R.color.selectedButtonColor));

            signInButton.setEnabled(true);
            signInButton.setBackgroundColor(getResources().getColor(R.color.deselectedButtonColor));
        } else {
            // Sign-In selected, disable Sign-In button, enable Sign-Up button
            signInButton.setEnabled(false);
            signInButton.setBackgroundColor(getResources().getColor(R.color.selectedButtonColor));

            signUpButton.setEnabled(true);
            signUpButton.setBackgroundColor(getResources().getColor(R.color.deselectedButtonColor));
        }
    }
}





