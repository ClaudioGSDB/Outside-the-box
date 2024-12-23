// Level1.java
package com.example.outsidethebox.levels;

import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.R;
import com.example.outsidethebox.level1resources.Level1Renderer;

import android.opengl.GLSurfaceView;
import android.widget.Button;

public class Level1 extends AppCompatActivity {
    private int levelNum;
    private GlobalVariables globalVariables;
    private Button completeButton;

    private Level1Renderer my_renderer;

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

        //setContentView(layoutResId);
        setContentView(R.layout.activity_level1); // Patch because findViewById throws error when looking for glsurfaceview specifically in level 1
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize GlobalVariables
        globalVariables = GlobalVariables.getInstance();

        // From GLSurfaceViewExample sample project
        GLSurfaceView surfaceView = findViewById(R.id.level1SurfaceView);
        surfaceView.setEGLContextClientVersion(3);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        surfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

        completeButton = findViewById(R.id.completeButton);
        completeButton.setVisibility(View.GONE);

        Handler handler = new Handler();
        my_renderer = new Level1Renderer(this, handler, () -> {
            completeButton.setVisibility(View.VISIBLE);
        });

        surfaceView.setRenderer(my_renderer);

        // Get current level number
        try {
            levelNum = Integer.parseInt(getClass().getSimpleName().replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            levelNum = 0; // Default to 0 for Level0
            Log.e("LevelActivity", "Could not parse level number", e);
        }
    }

    public void onComplete(View view) {
        // Mark level as completed
        globalVariables.setLevelComplete(levelNum);
        globalVariables.saveData(this);

        // Finish the activity
        finish();
    }
}