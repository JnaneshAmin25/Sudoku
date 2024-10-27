package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudoku.GameBoard.GameBoard;

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
        userIcon.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, ProfileActivity.class);
            startActivity(intent);
        });

        playNowButton.setOnClickListener(v->{
            Intent i = new Intent(this, GameBoard.class);
            startActivity(i);
        });

        helpButton.setOnClickListener(v->{
            Intent i1 = new Intent(this, helppage.class);
            startActivity(i1);
        });

    }
}
