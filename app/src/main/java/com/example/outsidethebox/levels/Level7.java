package com.example.outsidethebox.levels;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.R;

public class Level7 extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView hintText;
    private ImageView cracksOverlay;
    private FrameLayout fallingLinesView;
    private boolean isDropDetected = false;
    private static final float DROP_THRESHOLD = -1f; // Adjust this value based on testing
    private static final float SENSITIVITY = 0.5f;

    private GlobalVariables globalVariables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level7);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        globalVariables = GlobalVariables.getInstance();

        // Initialize UI elements
        hintText = findViewById(R.id.hintText);
        cracksOverlay = findViewById(R.id.cracksOverlay);
        fallingLinesView = findViewById(R.id.fallingLinesView);

        // Set initial hint text
        hintText.setText("d r o p   y o u r   p h o n e");
        hintText.setTextColor(Color.RED);
        hintText.setTextSize(35);

        // Setup sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listener
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Check for downward acceleration
        float zAcceleration = event.values[1]; // Y-axis for downward motion

        if (!isDropDetected && zAcceleration < DROP_THRESHOLD) {
            // Drop detected
            isDropDetected = true;

            // Animate falling lines
            animateFallingLines();

            // Show cracks after a short delay
            new Handler().postDelayed(this::showCracks, 1200);
        }
    }

    private void animateFallingLines() {
        // Create multiple falling line animations
        for (int i = 0; i < 10; i++) {
            View fallingLine = new View(this);
            fallingLine.setBackgroundColor(getResources().getColor(android.R.color.white));
            fallingLine.setLayoutParams(new LinearLayout.LayoutParams(5, 200)); // Thin white lines

            // Random horizontal position
            float startX = (float) (Math.random() * fallingLinesView.getWidth());
            float startY = (float) ((Math.random() * fallingLinesView.getHeight() + 200) + 1000);

            TranslateAnimation animation = new TranslateAnimation(
                    startX, startX,
                    startY, -200
            );
            animation.setDuration(500);
            fallingLine.startAnimation(animation);

            fallingLinesView.addView(fallingLine);
        }
    }

    private void showCracks() {
        // Make cracks overlay visible
        cracksOverlay.setVisibility(View.VISIBLE);

        // Complete level after showing cracks
        new Handler().postDelayed(() -> {
            onComplete(null);
        }, 2000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    public void onComplete(View view) {
        // Mark level as completed and finish
        globalVariables = GlobalVariables.getInstance();
        globalVariables.setLevelComplete(7);
        globalVariables.saveData(this);
        isDropDetected = false;
        finish();
    }
}