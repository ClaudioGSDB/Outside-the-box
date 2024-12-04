// Level4.java
package com.example.outsidethebox.levels;

import android.content.Context;
import android.graphics.Color;
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

public class Level4 extends AppCompatActivity {
    private int levelNum = 4;
    private GlobalVariables globalVariables;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView visualBox;
    private float lightThreshold = 40000.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level4);

        globalVariables = GlobalVariables.getInstance();

        visualBox = findViewById(R.id.visualBox);

        // Initialize Light Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        if (lightSensor == null) {
            Log.e("Level4", "No light sensor found");
        } else {
            sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private final SensorEventListener lightSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float lightIntensity = event.values[0];
                if (lightIntensity >= lightThreshold) {
                    visualBox.setText("Complete!");
                    visualBox.setBackgroundColor(Color.GREEN);

                    // Mark level complete and finish
                    onComplete(null);
                } else {
                    visualBox.setText("Too Dark");
                    visualBox.setBackgroundColor(Color.DKGRAY);
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
            sensorManager.unregisterListener(lightSensorListener);
        }
    }

    public void onComplete(View view) {
        globalVariables.setLevelComplete(levelNum);
        globalVariables.saveData(this);
        finish();
    }
}
