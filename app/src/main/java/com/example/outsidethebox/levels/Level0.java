// Level0.java
package com.example.outsidethebox.levels;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.R;

import java.util.ArrayList;

import MiniSprite.CustomTransforms.MiniSprite;
import MiniSprite.MiniSpriteSurface;
import MiniSprite.Transform2D;
import MiniSprite.IMiniSpriteSurfaceListener;

public class Level0 extends AppCompatActivity implements SensorEventListener, IMiniSpriteSurfaceListener {
    private int levelNum;
    private GlobalVariables globalVariables;

    private MiniSpriteSurface miniSpriteSurface;
    private boolean started;
    private final double TAU = Math.PI * 2;

    private ArrayList<Transform2D> petals = new ArrayList<>();

    private float[] gyroscopeXYZ = new float[3];

    private float rotationSpeed = 0;
    private float rotationSpeedDecay = 1f;
    private float maxRotationSpeed = 60;
    private float thresholdGyroscopeSpeed = 0.5f;
    private float completionGyroscopeSpeed = 7;
    private boolean completed;

    private Button completeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Dynamically get the layout resource ID
        int layoutResId = getResources().getIdentifier("activity_level" + levelNum, "layout", getPackageName());

        // Fallback to activity_level0 if specific layout doesn't exist
        if (layoutResId == 0) {
            layoutResId = R.layout.activity_level0;
        }

        setContentView(layoutResId);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        completeButton = findViewById(R.id.completeButton);
        completeButton.setVisibility(View.GONE);

        // Initialize GlobalVariables
        globalVariables = GlobalVariables.getInstance();

        SensorManager manager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(accelerometer != null){
            manager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL,SensorManager.SENSOR_DELAY_UI);
        }

        SurfaceView mySurface = findViewById(R.id.surfaceView);
        miniSpriteSurface = new MiniSpriteSurface(mySurface);
        miniSpriteSurface.addListener(this);

        // Get current level number
        try {
            levelNum = Integer.parseInt(getClass().getSimpleName().replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            levelNum = 0; // Default to 0 for Level0
            Log.e("LevelActivity", "Could not parse level number", e);
        }
    }

    @Override
    public void onPreUpdate(MiniSpriteSurface miniSpriteSurface, Canvas canvas) {
        if(started) return;

        int centerX = canvas.getWidth() / 2; // Center of screen
        int centerY = canvas.getHeight() / 2;
        double drawableDimensionWidthToHeightRatio = 1 / 2.24; // Petal drawable is 227 w 508 h.
        int height = (int)(canvas.getWidth() / 2.3); // 2 petals cover horizontal screen.
        int width = (int) (height * drawableDimensionWidthToHeightRatio);
        int anchorOffsetY = -height / 2; // Petal's bottom should be at the center.

        int petalCount = 10;

        for(int i = 0; i < petalCount; i++){
            double angleRads = (TAU / petalCount) * i;
            double angleDegrees = Math.toDegrees(angleRads);
            Transform2D petal = new MiniSprite.Builder(R.drawable.petal, getResources())
                    .withName("Petal")
                    .withDimensions(width, height)
                    .withPositionXY(centerX, centerY)
                    .withAnchorOffsetXY(0, anchorOffsetY)
                    .withRotationAngleDegrees((int) angleDegrees)
                    .build();

            petals.add(petal);
            miniSpriteSurface.AllTransforms.instantiateTransform(petal, 1);
        }

        int boxSide = (int)(canvas.getWidth() / 4);
        Transform2D box = new MiniSprite.Builder(R.drawable.box, getResources())
                .withName("Box")
                .withPositionXY(centerX, centerY)
                .withDimensions(boxSide, boxSide)
                .build();
        miniSpriteSurface.AllTransforms.instantiateTransform(box, 2);

        started = true;
    }

    @Override
    public void onPreDrawSprites(MiniSpriteSurface miniSpriteSurface, Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.black));
    }

    @Override
    public void onPostDrawSprites(MiniSpriteSurface miniSpriteSurface, Canvas canvas) {
        for (Transform2D petal : petals) {
            petal.RotationAngleDegrees += rotationSpeed;
        }

        if(rotationSpeed > rotationSpeedDecay){
            rotationSpeed -= rotationSpeedDecay;
        } else if(rotationSpeed < rotationSpeedDecay) {
            rotationSpeed += rotationSpeedDecay;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(completed) return;

        gyroscopeXYZ[0] = sensorEvent.values[0];
        gyroscopeXYZ[1] = sensorEvent.values[1];
        gyroscopeXYZ[2] = sensorEvent.values[2];

        if(Math.abs(gyroscopeXYZ[2]) > thresholdGyroscopeSpeed){
            rotationSpeed = -(gyroscopeXYZ[2]/completionGyroscopeSpeed) * maxRotationSpeed;
        }

        if(Math.abs(gyroscopeXYZ[2]) >= completionGyroscopeSpeed){
            completeButton.setVisibility(View.VISIBLE);
            rotationSpeedDecay = 0;
            completed = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onComplete(View view) {
        // Mark level as completed
        globalVariables.setLevelComplete(levelNum);
        globalVariables.saveData(this);

        // Finish the activity
        finish();
    }
}

