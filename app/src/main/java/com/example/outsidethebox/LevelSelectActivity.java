package com.example.outsidethebox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LevelSelectActivity extends AppCompatActivity {
    private GlobalVariables globalVariables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level_select);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize GlobalVariables
        globalVariables = GlobalVariables.getInstance();
        globalVariables.loadData(this);

        // Dynamically create level buttons
        createLevelButtons();
    }

    private void createLevelButtons() {
        LinearLayout levelButtonContainer = findViewById(R.id.levelButtonContainer);

        for (int i = 0; i < globalVariables.getTotalLevels(); i++) {
            final int levelNum = i;
            Button levelButton = new Button(this);

            // Set button text
            levelButton.setText("Level " + (levelNum + 1));

            // Style completed levels differently
            if (globalVariables.isLevelComplete(levelNum)) {
                levelButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            }

            // Set click listener to start the specific level
            levelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Dynamically load level class
                        Class<?> levelClass = Class.forName("com.example.outsidethebox.levels.Level" + levelNum);
                        Intent levelIntent = new Intent(LevelSelectActivity.this, levelClass);
                        startActivity(levelIntent);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Add button to the container
            levelButtonContainer.addView(levelButton);
        }
    }
}