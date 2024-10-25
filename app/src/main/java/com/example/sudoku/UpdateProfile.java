package com.example.sudoku;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_activity);

        storageReference = FirebaseStorage.getInstance().getReference("images");

        imageView = findViewById(R.id.img);
        EditText editName = findViewById(R.id.editname);
        EditText editMail = findViewById(R.id.editmail);
        DatePicker editDob = findViewById(R.id.editdob);
        editDob.setCalendarViewShown(false);
        editDob.setSpinnersShown(true);

        Spinner editGender = findViewById(R.id.editgender);
        String[] genderOptions = {"--","Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editGender.setAdapter(adapter);

        loadUserProfile(editName, editMail, editDob, editGender);

        imageView.setOnClickListener(view -> openGallery());

        RelativeLayout saveButton = findViewById(R.id.save);
        RelativeLayout backButton = findViewById(R.id.back);
        saveButton.setOnClickListener(view -> saveProfile(editName, editDob, editGender));
        backButton.setOnClickListener(v->{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

    }

    private void loadUserProfile(EditText editName, EditText editMail, DatePicker editDob, Spinner editGender) {
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
                            editName.setText(snapshot.child("username").getValue(String.class));
                            editMail.setText(email); // Keep the email as it is
                            String dobString = snapshot.child("dob").getValue(String.class);
                            if (dobString != null) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Adjust the format if needed
                                try {
                                    Date dobDate = dateFormat.parse(dobString);

                                    // Convert the Date to Calendar to get day, month, year
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(dobDate);

                                    int year = calendar.get(Calendar.YEAR);
                                    int month = calendar.get(Calendar.MONTH);
                                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                                    // Set date on DatePicker
                                    editDob.updateDate(year, month, day); // Assuming editDob is a DatePicker
                                } catch (ParseException e) {
                                    e.printStackTrace(); // Handle parsing error
                                }
                            }
                            String gender = snapshot.child("gender").getValue(String.class);
                            if (gender != null) {
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) editGender.getAdapter();
                                int position = adapter.getPosition(gender);
                                editGender.setSelection(position); // Set the selected item in the Spinner
                            }
                            else{
                                editGender.setSelection(0);
                            }
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

    private void saveProfile(EditText editName, DatePicker editDob, Spinner editGender) {
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
                            int day = editDob.getDayOfMonth();
                            int month = editDob.getMonth();
                            int year = editDob.getYear();
                            String dobString = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                            snapshot.getRef().child("dob").setValue(dobString);
                            String selectedGender = editGender.getSelectedItem().toString();
                            snapshot.getRef().child("gender").setValue(selectedGender);
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
