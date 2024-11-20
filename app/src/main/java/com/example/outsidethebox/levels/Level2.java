package com.example.outsidethebox.levels;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.R;
import com.bumptech.glide.Glide;

public class Level2 extends AppCompatActivity {
    private final int levelNum = 2;
    private GlobalVariables globalVariables;
    private boolean initialWifiState;
    private boolean levelCompleted = false;
    private final Handler wifiCheckHandler = new Handler();
    private Button completeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        globalVariables = GlobalVariables.getInstance();

        ImageView wifiGif = findViewById(R.id.wifiGif);
        completeButton = findViewById(R.id.completeButton);

        Glide.with(this)
                .asGif()
                .load(R.drawable.wifi_dark)
                .into(wifiGif);

        initialWifiState = isWifiConnected();

        startWifiCheck();
    }

    private boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    private void startWifiCheck() {
        Runnable wifiCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (!levelCompleted) {
                    boolean currentWifiState = isWifiConnected();
                    Log.d("TEST", "Current: " + currentWifiState);
                    Log.d("TEST", "Initial: " + initialWifiState);
                    if (currentWifiState != initialWifiState) {
                        levelCompleted = true;
                        completeButton.setVisibility(View.VISIBLE);
                    }
                    wifiCheckHandler.postDelayed(this, 1000);
                }
            }
        };
        wifiCheckHandler.post(wifiCheckRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiCheckHandler.removeCallbacksAndMessages(null);
    }

    public void onComplete(View view) {
        if (levelCompleted) {
            globalVariables.setLevelComplete(levelNum);
            globalVariables.saveData(this);
            finish();
        }
    }
}