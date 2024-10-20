package com.example.sudoku;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UpdateProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("images");

        // Find the ImageView and EditText fields
        imageView = findViewById(R.id.img);
        EditText editName = findViewById(R.id.editname);
        EditText editMail = findViewById(R.id.editmail);
        EditText editDob = findViewById(R.id.editdob);
        EditText editGender = findViewById(R.id.editgender);

        // Load user profile information from Firebase
        loadUserProfile(editName, editMail, editDob, editGender);

        // Set click listener on ImageView to open gallery
        imageView.setOnClickListener(view -> openGallery());

        // Save button click listener
        AppCompatButton saveButton = findViewById(R.id.save);
        AppCompatButton backButton = findViewById(R.id.back);
        saveButton.setOnClickListener(view -> saveProfile(editName, editDob, editGender));
        backButton.setOnClickListener(v->{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

    }

    private void loadUserProfile(EditText editName, EditText editMail, EditText editDob, EditText editGender) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null && email.equals(currentUserEmail)) {
                            editName.setText(snapshot.child("name").getValue(String.class));
                            editMail.setText(email); // Keep the email as it is
                            editDob.setText(snapshot.child("dob").getValue(String.class));
                            editGender.setText(snapshot.child("gender").getValue(String.class));
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateProfile.this, "Error fetching profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri); // Set the selected image to ImageView
            uploadImageToFirebase(imageUri); // Call method to upload the image
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(UpdateProfile.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                // Optionally, get the download URL and store it in the database
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Store the download URL in the database if needed
                    // You can update the user's profile with this URL
                    saveImageUrlToDatabase(uri.toString());
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(UpdateProfile.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null && email.equals(currentUserEmail)) {
                            snapshot.getRef().child("profileImageUrl").setValue(imageUrl);
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateProfile.this, "Error updating image URL", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProfile(EditText editName, EditText editDob, EditText editGender) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null && email.equals(currentUserEmail)) {
                            snapshot.getRef().child("name").setValue(editName.getText().toString());
                            snapshot.getRef().child("dob").setValue(editDob.getText().toString());
                            snapshot.getRef().child("gender").setValue(editGender.getText().toString());

                            Toast.makeText(UpdateProfile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateProfile.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
