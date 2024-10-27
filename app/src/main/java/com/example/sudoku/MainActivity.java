package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager; // Facebook CallbackManager
    private DatabaseReference databaseReference;
    Button signUpButton;
    Button signInButton;
    TextView welcomeText;
    RelativeLayout googleLogoGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Initialize Facebook CallbackManager
        callbackManager = CallbackManager.Factory.create();

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
        googleLogoGroup = findViewById(R.id.googleLogoGroup); // Find the Google sign-in button
       // Find the Facebook sign-in button
        welcomeText = findViewById(R.id.welcome_bac);

        // Set default color for the buttons
        signInButton.setBackgroundColor(getResources().getColor(R.color.selectedButtonColor));
        signUpButton.setBackgroundColor(getResources().getColor(R.color.deselectedButtonColor));
        signInButton.setEnabled(false);
        welcomeText.setText(R.string.welcome_bac);

        signUpButton.setOnClickListener(v -> {
            loadFragment(new SignUpFragment(), true);
            updateButtonState(true);
            welcomeText.setText(R.string.create_acc);
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
        googleLogoGroup.setOnClickListener(v -> {
            googleLogoGroup.setBackgroundResource(R.drawable.selected_rounded_corner);
            signInWithGoogle();
            new android.os.Handler().postDelayed(() -> {
                googleLogoGroup.setBackgroundResource(R.drawable.rounded_google_corner); // Restore original background
            }, 70);
        });



        // Register Facebook callback
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Handle Facebook access token
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                ToastUtils.showToast(MainActivity.this, "Facebook sign in canceled", 2000); // 1 second duration
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookLoginError", "Error during Facebook login: ", error);
                ToastUtils.showToast(MainActivity.this, "Facebook sign in failed: " + error.getMessage(), 2000); // 1 second duration
            }
        });
    }

    // Method to initiate Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Handle the result of Google Sign-In and Facebook Sign-In
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook Login result
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Google Sign-In result
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed", e);
                ToastUtils.showToast(MainActivity.this, "Google sign in failed", 2000); // 1 second duration
            }
        }
    }

    // Authenticate with Firebase using the Google account
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        saveUserToDatabase(auth.getUid(), user.getEmail());
                        ToastUtils.showToast(MainActivity.this, "Google sign in successful", 2000); // 1 second duration
                        updateUI(user);
                    } else {
                        ToastUtils.showToast(MainActivity.this, "Authentication failed", 2000); // 1 second duration
                        updateUI(null);
                    }
                });
    }

    // Handle Facebook Access Token
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        saveUserToDatabase(auth.getUid(), user.getEmail());
                        ToastUtils.showToast(MainActivity.this, "Facebook sign in successful", 2000); // 1 second duration
                        updateUI(user);
                    } else {
                        ToastUtils.showToast(MainActivity.this, "Authentication failed", 2000); // 1 second duration
                        updateUI(null);
                    }
                });
    }

    private void saveUserToDatabase(String uid, String email) {
        // Create a user object
        Users user = new Users(uid, email);

        // Store user info under the user's UID in the database
        databaseReference.child(uid).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ToastUtils.showToast(this, "User data saved to database.", 1000); // 1 second duration
                    } else {
                        ToastUtils.showToast(this, "Failed to save user data.", 1000); // 1 second duration
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }

    private void loadFragment(Fragment fragment, boolean isSignUp) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void updateButtonState(boolean isSignUpSelected) {
        if (isSignUpSelected) {
            signUpButton.setEnabled(false);
            signUpButton.setBackgroundColor(getResources().getColor(R.color.selectedButtonColor));
            signInButton.setEnabled(true);
            signInButton.setBackgroundColor(getResources().getColor(R.color.deselectedButtonColor));
        } else {
            signInButton.setEnabled(false);
            signInButton.setBackgroundColor(getResources().getColor(R.color.selectedButtonColor));
            signUpButton.setEnabled(true);
            signUpButton.setBackgroundColor(getResources().getColor(R.color.deselectedButtonColor));
        }
    }
}

