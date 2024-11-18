package com.example.outsidethebox.levels;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.LevelSelectActivity;
import com.example.outsidethebox.R;

import java.util.Arrays;

public class Level0 extends AppCompatActivity {

    int levelNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level0);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get current level number
        String className = this.getClass().getSimpleName();
        String[] parts = className.split("Level");
        levelNum = Integer.parseInt(parts[1]);
    }

    public void onComplete(View view) throws ClassNotFoundException {
        // mark level as completed
        GlobalVariables.levels[levelNum] = true;
        GlobalVariables.SaveData(this);
        Log.d("DataStorageService","Levels = "+ Arrays.toString(GlobalVariables.levels));

        // increment to the next level unless doesn't exist
        levelNum+=1;
        if(levelNum > GlobalVariables.levels.length){
            this.finish();
        }
        Class<?> className = Class.forName("com.example.outsidethebox.levels.Level" + levelNum);
        Intent myIntent = new Intent(this, className);

        this.startActivity(myIntent);
        this.finish();
    }
}