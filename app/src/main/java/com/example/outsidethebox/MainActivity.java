package com.example.outsidethebox;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public static boolean[] levels;

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

        // load levels
        GlobalVariables.LoadData(this);
        levels = GlobalVariables.levels;
        Log.d("DataStorageService","Levels = "+ Arrays.toString(levels));
    }

    public void onPlay(View view) throws ClassNotFoundException {
        // find first level that's not complete
        int recent = 0;
        for(; recent < levels.length; recent++){
            if(!levels[recent]){
                break;
            }
        }

        // determine which activity to start based on: "Level" + recent
        Class<?> className = Class.forName("com.example.outsidethebox.levels.Level" + recent);
        Intent myIntent = new Intent(this, className);

        this.startActivity(myIntent);
    }

    public void onLevelSelect(View view){
        Intent myIntent = new Intent(this, LevelSelectActivity.class);
        this.startActivity(myIntent);
    }

    @Override
    protected void onPause(){

        GlobalVariables.SaveData(this);

        super.onPause();
    }
}