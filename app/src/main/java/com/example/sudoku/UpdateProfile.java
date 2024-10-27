package com.example.sudoku;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateProfile extends AppCompatActivity {

    private ImageView imageView;
    private RelativeLayout save,back;
    private RadioGroup radioGroup;
    public static String gender;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private Uri imageUri; // To store image URI
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_activity);

        storageReference = FirebaseStorage.getInstance().getReference("images");

        imageView = findViewById(R.id.img);
        imageView.setOnClickListener(v -> showImageSourceDialog());

        EditText editName = findViewById(R.id.editname);
        EditText editMail = findViewById(R.id.editmail);
        DatePicker editDob = findViewById(R.id.editdob);
        editDob.setCalendarViewShown(false);
        editDob.setSpinnersShown(true);
        radioGroup = findViewById(R.id.radioGroupGender);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            gender = (checkedId == R.id.radioMale) ? "Male" :
                    (checkedId == R.id.radioFemale) ? "Female" : "";

        });

        loadUserProfile(editName, editMail, editDob,radioGroup,imageView);

        save = findViewById(R.id.save);
        back = findViewById(R.id.back);
        save.setOnClickListener(view -> {
            Toast.makeText(this, "Profile Updated Succesfully", Toast.LENGTH_SHORT).show();
            saveProfile(editName, editDob);
        });
        back.setOnClickListener(v->{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

    }

    private void loadUserProfile(EditText editName, EditText editMail, DatePicker editDob, RadioGroup radioGroup, ImageView profileImageView) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();  // Get current user's unique ID
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Set name
                    editName.setText(dataSnapshot.child("name").getValue(String.class));

                    // Set email and make it non-editable
                    String email = dataSnapshot.child("email").getValue(String.class);
                    editMail.setText(email);
                    editMail.setEnabled(false);

                    // Parse and set date of birth
                    String dobString = dataSnapshot.child("dob").getValue(String.class);
                    if (dobString != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        try {
                            Date dobDate = dateFormat.parse(dobString);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dobDate);
                            editDob.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    if ("Male".equals(gender)) {
                        radioGroup.check(R.id.radioMale);
                    } else if ("Female".equals(gender)) {
                        radioGroup.check(R.id.radioFemale);
                    }

                    // Fetch and display profile image
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(UpdateProfile.this)
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.baseline_image_search_24)
                                .into(profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateProfile.this, "Error fetching profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProfile(EditText editName, DatePicker editDob) {
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
                            snapshot.getRef().child("name").setValue(editName.getText().toString());

                            int day = editDob.getDayOfMonth();
                            int month = editDob.getMonth();
                            int year = editDob.getYear();
                            String dobString = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                            snapshot.getRef().child("dob").setValue(dobString);

                            // Get selected gender from RadioGroup
                            int selectedGenderId = radioGroup.getCheckedRadioButtonId();
                            RadioButton selectedGenderButton = findViewById(selectedGenderId);
                            if (selectedGenderButton != null) {
                                String selectedGender = selectedGenderButton.getText().toString();
                                snapshot.getRef().child("gender").setValue(selectedGender);
                            }

                            Toast.makeText(UpdateProfile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            break; // Exit the loop after updating the user
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

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_REQUEST);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Create a file for storing the image
            File photoFile = new File(getExternalFilesDir(null), "photo_" + System.currentTimeMillis() + ".jpg");
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);

            // Pass the content URI instead of a file URI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri); // Set the selected image to ImageView
            uploadImageToFirebase(imageUri); // Call method to upload the image
        } else if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageView.setImageURI(imageUri); // Set the captured image to ImageView
            uploadImageToFirebase(imageUri); // Call method to upload the image
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.setMessage("Please wait while we upload your image.");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateProfile.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> saveImageUrlToDatabase(uri.toString()));
                    })
                    .addOnProgressListener(taskSnapshot->{
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + (int) progress + "%...");
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateProfile.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });


        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAPTURE_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(); // Permission granted, open the camera
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
