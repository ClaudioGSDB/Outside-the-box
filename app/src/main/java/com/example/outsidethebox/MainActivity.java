package com.example.outsidethebox;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private boolean[] levels;
    private GlobalVariables globalVariables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize GlobalVariables
        globalVariables = GlobalVariables.getInstance();

        // Load level data
        globalVariables.loadData(this);
        levels = globalVariables.getLevels();

        Log.d("DataStorageService", "Levels = " + Arrays.toString(levels));

        // Update level icons based on completion
        updateLevelIcons();
    }

    private void updateLevelIcons() {
        // Update icons for all 10 levels
        for (int i = 0; i < 10; i++) {
            int resId = getResources().getIdentifier("level" + i + "_icon", "id", getPackageName());
            if (resId != 0) {
                ImageView levelIcon = findViewById(resId);
                if (levelIcon != null) {
                    // Change the icon opacity or add a completed overlay if the level is complete
                    levelIcon.setAlpha(levels[i] ? 0.5f : 1.0f);
                }
            }
        }
    }

    public void onPlay(View view) {
        try {
            // Find first level that's not complete
            int recent = 0;
            for (; recent < levels.length; recent++) {
                if (!levels[recent]) {
                    break;
                }
            }

            // Ensure we don't go out of bounds
            if (recent >= levels.length) {
                // All levels completed
                return;
            }

            // Dynamically load level class
            Class<?> levelClass = Class.forName("com.example.outsidethebox.levels.Level" + recent);
            Intent levelIntent = new Intent(this, levelClass);
            startActivity(levelIntent);

        } catch (ClassNotFoundException e) {
            Log.e("MainActivity", "Level class not found", e);
            // Optionally show a toast or dialog to the user
        }
    }

    public void onLevelSelect(View view) {
        Intent myIntent = new Intent(this, LevelSelectActivity.class);
        this.startActivity(myIntent);
    }

    @Override
    protected void onPause() {
        // Save data when the activity is paused
        globalVariables.saveData(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Reload level data and update icons when returning to the main activity
        globalVariables.loadData(this);
        levels = globalVariables.getLevels();
        updateLevelIcons();
        super.onResume();
    }

    public void onResetProgress(View view) {
        // Get the GlobalVariables instance
        GlobalVariables globalVariables = GlobalVariables.getInstance();

        // Reset all levels to false
        boolean[] levels = globalVariables.getLevels();
        for (int i = 0; i < levels.length; i++) {
            levels[i] = false;
        }

        // Save the reset data
        globalVariables.saveData(this);

        // Update the level icons to reflect the reset
        updateLevelIcons();
    }
}