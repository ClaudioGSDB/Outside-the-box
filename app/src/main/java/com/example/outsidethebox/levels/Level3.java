package com.example.outsidethebox.levels;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.outsidethebox.GlobalVariables;
import com.example.outsidethebox.R;
import com.example.outsidethebox.levels.util.DrawingView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class Level3 extends AppCompatActivity {
    private final int levelNum = 3;
    private GlobalVariables globalVariables;
    private DrawingView drawingView;
    private Button completeButton;
    private TextRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level3);

        globalVariables = GlobalVariables.getInstance();
        drawingView = findViewById(R.id.drawingView);
        completeButton = findViewById(R.id.completeButton);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(v -> {
            drawingView.clear();
            completeButton.setVisibility(View.GONE);
        });

        drawingView.setOnTouchListener((v, event) -> {
            v.onTouchEvent(event);
            checkText();
            return true;
        });
    }

    private void checkText() {
        Bitmap bitmap = drawingView.getBitmap();
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    String recognizedText = text.getText().toLowerCase().trim();
                    if (recognizedText.contains("box")) {
                        completeButton.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void onComplete(View view) {
        globalVariables.setLevelComplete(levelNum);
        globalVariables.saveData(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognizer.close();
    }
}