package com.example.outsidethebox.levels;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;

import java.util.Random;

public class Level9 extends AppCompatActivity {
    private AudioVisualizerView visualizerView;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        visualizerView = new AudioVisualizerView(this);
        setContentView(visualizerView);

        ViewCompat.setOnApplyWindowInsetsListener(visualizerView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private class AudioVisualizerView extends View {
        private static final int TARGET_VOLUME = 30;
        private static final int VOLUME_THRESHOLD = 5;
        private static final int NUM_BARS = 32;
        private static final float BAR_MAX_HEIGHT = 400f;
        private static final float BAR_MIN_HEIGHT = 10f;
        private static final float BAR_WIDTH = 16f;
        private static final float BAR_SPACING = 8f;
        private float[] noiseOffsets; // Store noise offsets for each bar

        private Paint barPaint;
        private float[] barHeights;
        private float[] targetHeights;
        private Random random;
        private int currentVolume = 0;
        private float smoothedVolume = 0f;

        private final int BASE_COLOR = Color.parseColor("#E0B0FF"); // Light purple
        private final int ACTIVE_COLOR = Color.parseColor("#9932CC"); // Dark purple

        public AudioVisualizerView(Context context) {
            super(context);
            init();
        }

        private void init() {
            barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            barPaint.setStyle(Paint.Style.FILL);

            random = new Random();
            barHeights = new float[NUM_BARS];
            targetHeights = new float[NUM_BARS];
            noiseOffsets = new float[NUM_BARS];

            // Initialize random offsets for each bar
            for (int i = 0; i < NUM_BARS; i++) {
                noiseOffsets[i] = random.nextFloat() * 1000;
            }

            setupAnimation();
            startVolumeMonitoring();
        }

        private void setupAnimation() {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(50);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setInterpolator(new LinearInterpolator());

            animator.addUpdateListener(animation -> {
                updateBars();
                invalidate();
            });

            animator.start();
        }

        private void updateBars() {
            float volumeDifference = Math.abs(smoothedVolume - TARGET_VOLUME);
            float intensityFactor;

            // Smooth transition based on distance from target
            if (volumeDifference <= VOLUME_THRESHOLD) {
                intensityFactor = 0.4f + (0.6f * (1.0f - volumeDifference / VOLUME_THRESHOLD));
            } else {
                float distanceFactor = Math.min(volumeDifference / (VOLUME_THRESHOLD * 3), 1.0f);
                intensityFactor = 0.4f * (1.0f - distanceFactor);
            }

            float time = System.currentTimeMillis() * 0.001f;

            for (int i = 0; i < NUM_BARS; i++) {
                // Update noise offset for this bar
                noiseOffsets[i] += 0.02f;

                // Generate noise value using offset
                float noise = (float) (Math.sin(noiseOffsets[i]) * 0.5f +
                        Math.sin(noiseOffsets[i] * 2.1f) * 0.3f +
                        Math.sin(noiseOffsets[i] * 4.2f) * 0.2f);

                // Add random spikes occasionally
                if (random.nextFloat() < 0.02f) {
                    noise *= 1.5f;
                }

                // Normalize noise to 0-1 range
                float normalizedNoise = (noise + 1f) * 0.5f;

                // Calculate height with more dramatic variations
                float baseHeight = BAR_MIN_HEIGHT + (BAR_MAX_HEIGHT - BAR_MIN_HEIGHT) * intensityFactor;
                float targetHeight = baseHeight * (0.3f + normalizedNoise * 0.7f);

                // Add influence from neighbors for smoother transitions
                if (i > 0 && i < NUM_BARS - 1) {
                    float neighborAverage = (barHeights[i-1] + barHeights[i+1]) * 0.5f;
                    targetHeight = targetHeight * 0.7f + neighborAverage * 0.3f;
                }

                targetHeights[i] = targetHeight;

                // Smooth transition to target height
                float transitionSpeed = 0.15f;
                float diff = targetHeights[i] - barHeights[i];
                barHeights[i] += diff * transitionSpeed;
            }
        }


        private void startVolumeMonitoring() {
            post(new Runnable() {
                @Override
                public void run() {
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentVolume = (volume * 100) / maxVolume;

                    // Smoother volume changes
                    smoothedVolume += (currentVolume - smoothedVolume) * 0.1f;

                    // Check for level completion
                    float volumeDifference = Math.abs(smoothedVolume - TARGET_VOLUME);
                    if (volumeDifference <= 1) {
                        GlobalVariables.getInstance().setLevelComplete(9);
                        GlobalVariables.getInstance().saveData(getContext());
                    }

                    postDelayed(this, 50);
                }
            });
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float volumeDifference = Math.abs(smoothedVolume - TARGET_VOLUME);
            float colorRatio = volumeDifference <= VOLUME_THRESHOLD ?
                    (float) Math.pow(1.0f - (volumeDifference / VOLUME_THRESHOLD), 2) : 0f;

            // Calculate total width needed
            float totalWidth = NUM_BARS * (BAR_WIDTH + BAR_SPACING) - BAR_SPACING;
            float startX = (getWidth() - totalWidth) / 2;
            float centerY = getHeight() / 2;

            for (int i = 0; i < NUM_BARS; i++) {
                float height = barHeights[i];
                float x = startX + i * (BAR_WIDTH + BAR_SPACING);

                // Blend between base and active colors
                barPaint.setColor(blendColors(BASE_COLOR, ACTIVE_COLOR, colorRatio));

                // Draw bar extending up and down from center
                canvas.drawRect(
                        x,
                        centerY - height / 2,
                        x + BAR_WIDTH,
                        centerY + height / 2,
                        barPaint
                );
            }
        }

        private int blendColors(int color1, int color2, float ratio) {
            float inverseRatio = 1f - ratio;
            float r = (Color.red(color1) * inverseRatio + Color.red(color2) * ratio);
            float g = (Color.green(color1) * inverseRatio + Color.green(color2) * ratio);
            float b = (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio);
            return Color.rgb((int) r, (int) g, (int) b);
        }
    }
}