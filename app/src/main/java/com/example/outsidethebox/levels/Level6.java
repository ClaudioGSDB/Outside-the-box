package com.example.outsidethebox.levels;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.R;

public class Level6 extends AppCompatActivity implements SensorEventListener {
    private int levelNum;
    private GlobalVariables globalVariables;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private ImageView lockDialView;
    private TextView instructionTextView;

    // Lock combination settings
    private int[] correctCombination = {90, 180, 270}; // Degrees to rotate for each stage
    private int currentStage = 0;
    private float currentRotation = 0f;

    private Vibrator vibrator;

    // Rotation thresholds and sensitivity
    private static final float ROTATION_SENSITIVITY = 3.0f;
    private static final float MAX_ROTATION = 360f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock orientation to portrait for this specific level
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level6);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        lockDialView = findViewById(R.id.lockDialView);
        instructionTextView = findViewById(R.id.instructionTextView);

        // Setup sensor and vibration services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize GlobalVariables
        globalVariables = GlobalVariables.getInstance();

        // Update initial instruction
        updateInstructions();
    }

    private void updateInstructions() {
        String[] stageInstructions = {
                "Rotate phone 90 degrees clockwise",
                "Rotate phone 180 degrees clockwise",
                "Rotate phone 90 degrees counter clockwise"
        };

        if (currentStage < stageInstructions.length) {
            instructionTextView.setText(stageInstructions[currentStage]);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register accelerometer listener
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listener to save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get phone orientation using accelerometer
            float x = event.values[0]; // Horizontal tilt
            float y = event.values[1]; // Vertical tilt


            // Calculate rotation based on phone tilt
            // Use arctan to get angle, convert to degrees
            float rotation = (float) Math.toDegrees(Math.atan2(x, y));

            // Normalize rotation to 0-360 degrees
            rotation = (rotation + 360) % 360;

            // Apply sensitivity threshold
            if (Math.abs(rotation - currentRotation) > ROTATION_SENSITIVITY) {
                // Animate lock dial rotation
                RotateAnimation rotateAnimation = new RotateAnimation(
                        currentRotation,
                        rotation,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f
                );
                rotateAnimation.setDuration(100);
                rotateAnimation.setFillAfter(true);
                lockDialView.startAnimation(rotateAnimation);

                // Update current rotation
                currentRotation = rotation;

                // Check if rotation matches the current stage's requirement
                if (Math.abs(currentRotation - correctCombination[currentStage]) < 10) {
                    // Stage completed
                    performStageCompletion();
                }
            }
        }
    }

    private void performStageCompletion() {
        // Vibrate to indicate correct rotation
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // Move to next stage
        currentStage++;

        // Check if all stages are complete
        if (currentStage == correctCombination.length) {
            // Level complete
            onComplete(null);
        } else {
            // Update instructions for next stage
            updateInstructions();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    public void onComplete(View view) {
        // Mark level as completed and finish
        globalVariables = GlobalVariables.getInstance();
        globalVariables.setLevelComplete(6);
        globalVariables.saveData(this);
        finish();
    }
}