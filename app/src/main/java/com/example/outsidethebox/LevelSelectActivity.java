package com.example.outsidethebox;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class LevelSelectActivity extends AppCompatActivity {
    private GlobalVariables globalVariables;
    private GridLayout levelButtonContainer;
    private static final int BUTTON_MARGIN = 8;
    private static final int[] LEVEL_COLORS = {
            Color.parseColor("#4CAF50"),  // Green
            Color.parseColor("#2196F3"),  // Blue
            Color.parseColor("#FF9800"),  // Orange
            Color.parseColor("#E91E63"),  // Pink
            Color.parseColor("#9C27B0")   // Purple
    };

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

        globalVariables = GlobalVariables.getInstance();
        levelButtonContainer = findViewById(R.id.levelButtonContainer);
        createLevelButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data and refresh the view
        globalVariables.loadData(this);
        refreshLevelButtons();
    }

    private void refreshLevelButtons() {
        // Clear existing buttons
        levelButtonContainer.removeAllViews();
        // Recreate all buttons
        createLevelButtons();
    }

    private void createLevelButtons() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int buttonWidth = (screenWidth - 48) / 2; // Account for padding and margins

        for (int i = 0; i < globalVariables.getTotalLevels(); i++) {
            final int levelNum = i;
            MaterialCardView cardView = createLevelCard(levelNum, buttonWidth);

            // Add card to container
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = buttonWidth;
            params.height = buttonWidth;
            params.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
            cardView.setLayoutParams(params);

            levelButtonContainer.addView(cardView);
        }
    }

    private MaterialCardView createLevelCard(int levelNum, int width) {
        MaterialCardView cardView = new MaterialCardView(this);
        cardView.setCardBackgroundColor(Color.parseColor("#1E1E1E"));
        cardView.setRadius(width / 8f);
        cardView.setCardElevation(8);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        // Set ripple color based on completion status
        int accentColor = LEVEL_COLORS[levelNum % LEVEL_COLORS.length];
        cardView.setRippleColor(createRippleColorList(accentColor));

        // Create content layout
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Level number
        TextView levelText = new TextView(this);
        levelText.setText(String.valueOf(levelNum + 1));
        levelText.setTextSize(32);
        levelText.setTextColor(accentColor);
        levelText.setGravity(Gravity.CENTER);

        // Level label
        TextView labelText = new TextView(this);
        labelText.setText("LEVEL");
        labelText.setTextSize(14);
        labelText.setTextColor(Color.parseColor("#979797"));
        labelText.setGravity(Gravity.CENTER);

        // Add completion indicator if level is completed
        if (globalVariables.isLevelComplete(levelNum)) {
            cardView.setStrokeWidth(4);
            cardView.setStrokeColor(accentColor);
        }

        contentLayout.addView(levelText);
        contentLayout.addView(labelText);
        cardView.addView(contentLayout);

        // Set click listener
        cardView.setOnClickListener(v -> {
            try {
                Class<?> levelClass = Class.forName("com.example.outsidethebox.levels.Level" + levelNum);
                Intent levelIntent = new Intent(LevelSelectActivity.this, levelClass);
                startActivity(levelIntent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        return cardView;
    }

    private android.content.res.ColorStateList createRippleColorList(int color) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed },
                new int[] {}
        };

        int[] colors = new int[] {
                adjustAlpha(color, 0.2f),
                Color.TRANSPARENT
        };

        return new android.content.res.ColorStateList(states, colors);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}