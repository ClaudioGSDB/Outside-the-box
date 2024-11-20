package com.example.outsidethebox;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class GlobalVariables {
    private static final String PREFS_NAME = "GameLevelsPrefs";
    private static final int NUM_OF_LEVELS = 10; // Adjust this to the total number of levels in your game

    // Singleton-like approach to ensure consistent access
    private static GlobalVariables instance;

    // Level completion status
    private boolean[] levels;

    private GlobalVariables() {
        // Initialize levels array with false
        levels = new boolean[NUM_OF_LEVELS];
    }

    // Singleton-like method to get instance
    public static GlobalVariables getInstance() {
        if (instance == null) {
            instance = new GlobalVariables();
        }
        return instance;
    }

    // Save level completion data using SharedPreferences
    public void saveData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < levels.length; i++) {
            editor.putBoolean("level_" + i, levels[i]);
        }

        editor.apply();
        Log.d("DataStorageService", "Levels saved: " + java.util.Arrays.toString(levels));
    }

    // Load level completion data
    public void loadData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        for (int i = 0; i < levels.length; i++) {
            levels[i] = prefs.getBoolean("level_" + i, false);
        }

        Log.d("DataStorageService", "Levels loaded: " + java.util.Arrays.toString(levels));
    }

    // Getter for levels
    public boolean[] getLevels() {
        return levels;
    }

    // Method to mark a level as complete
    public void setLevelComplete(int levelNum) {
        if (levelNum >= 0 && levelNum < levels.length) {
            levels[levelNum] = true;
        }
    }

    // Method to check if a level is complete
    public boolean isLevelComplete(int levelNum) {
        if (levelNum >= 0 && levelNum < levels.length) {
            return levels[levelNum];
        }
        return false;
    }

    // Total number of levels
    public int getTotalLevels() {
        return levels.length;
    }
}