// Level5.java
package com.example.outsidethebox.levels;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.R;

public class Level5 extends AppCompatActivity {
    private int levelNum = 5;
    private GlobalVariables globalVariables;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView shakeBox;
    private int shakeCount = 0;
    private float shakeThreshold = 15.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level5);

        globalVariables = GlobalVariables.getInstance();
        shakeBox = findViewById(R.id.shakeBox);

        // Initialize Accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Log.e("Level5", "No accelerometer found");
        } else {
            sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private final SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float shakeMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

                if (shakeMagnitude > shakeThreshold) {
                    shakeCount++;
                    shakeBox.setTranslationY(shakeBox.getTranslationY() + 50); // Move box down

                    if (shakeCount >= 10) {
                        shakeBox.setText("Complete!");
                        onComplete(null);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(accelerometerListener);
        }
    }

    public void onComplete(View view) {
        globalVariables.setLevelComplete(levelNum);
        globalVariables.saveData(this);
        finish();
    }
}
