package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    private Button playNowButton;
    private Button helpButton;
    private ImageView userIcon;
    private ImageView paletteIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        playNowButton = findViewById(R.id.play_now);
        helpButton = findViewById(R.id.help);
        userIcon = findViewById(R.id.user_icon);
        paletteIcon = findViewById(R.id.palette_icon);



        // Handle User Icon click to navigate to ProfileActivity
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePage.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

    }
}
