package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class helppage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helppage);

        // Set image resources for help images
        ImageView helpIcon1 = findViewById(R.id.helpIcon);
        helpIcon1.setImageResource(R.drawable.baseline_home_filled_24);
        ImageView helpIcon = findViewById(R.id.helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the home page
                Intent intent = new Intent(helppage.this, HomePage.class);
                startActivity(intent);
            }
        });

        ImageView helpImage1 = findViewById(R.id.helpImage1);
        helpImage1.setImageResource(R.drawable.img);

        ImageView helpImage2 = findViewById(R.id.helpImage2);
        helpImage2.setImageResource(R.drawable.img1);
        ImageView helpImage4 = findViewById(R.id.helpImage3);
        helpImage4.setImageResource(R.drawable.img2);

        ImageView helpImage3 = findViewById(R.id.helpImage4);
        helpImage3.setImageResource(R.drawable.img3);
    }
}