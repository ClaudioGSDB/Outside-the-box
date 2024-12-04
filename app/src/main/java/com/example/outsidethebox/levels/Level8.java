package com.example.outsidethebox.levels;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.outsidethebox.GlobalVariables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Level8 extends AppCompatActivity {
    private BatteryPuzzleView batteryPuzzleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        batteryPuzzleView = new BatteryPuzzleView(this);
        setContentView(batteryPuzzleView);

        ViewCompat.setOnApplyWindowInsetsListener(batteryPuzzleView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private class BatteryPuzzleView extends View {
        private Paint backgroundPaint;
        private Paint foregroundPaint;
        private Paint foregroundGlowPaint;
        private Paint thresholdPaint;
        private Paint thresholdGlowPaint;
        private RectF batteryRect;
        private int currentBatteryLevel;
        private int thresholdLevel = 75;
        private float thresholdSparkPosition = 0;
        private float thresholdSparkVelocity = 15;
        private float foregroundSparkPosition = 0;
        private float foregroundSparkVelocity = 15;
        private static final float SPARK_SIZE = 20;
        private Random random = new Random();
        private BatteryLevelReceiver batteryLevelReceiver;
        private List<LightningBolt> lightningBolts = new ArrayList<>();
        private long lastLightningTime = 0;

        // Color constants
        private final int RAZER_GREEN = Color.parseColor("#44FF00");
        private final int ELECTRIC_YELLOW = Color.parseColor("#FFFF00");
        private final int DEEP_BLACK = Color.parseColor("#000000");

        private final int WHITE = Color.parseColor("#FFFFFF");
        private final int ELECTRIC_BLUE = Color.parseColor("#00F0FF"); // Bright cyan-blue
        private final int THRESHOLD_GRAY = Color.parseColor("#888888"); // Medium gray

        private class Point {
            float x, y;
            Point(float x, float y) {
                this.x = x;
                this.y = y;
            }
        }

        private class LightningSegment {
            float x1, y1, x2, y2;
            float brightness;

            LightningSegment(float x1, float y1, float x2, float y2, float brightness) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
                this.brightness = brightness;
            }
        }

        private class LightningBolt {
            float startX, startY, endX, endY;
            List<LightningSegment> segments = new ArrayList<>();
            float growthProgress = 0;
            long startTime;
            int numBranches;

            LightningBolt(float startX, float startY, float endX, float endY) {
                this.startX = startX;
                this.startY = startY;
                this.endX = endX;
                this.endY = endY;
                this.startTime = System.currentTimeMillis();
                this.numBranches = 2 + random.nextInt(3);
                generateSegments();
            }

            void generateSegments() {
                List<Point> mainPoints = generateBoltPoints(startX, startY, endX, endY, 0.3f);
                for (int i = 0; i < mainPoints.size() - 1; i++) {
                    segments.add(new LightningSegment(
                            mainPoints.get(i).x, mainPoints.get(i).y,
                            mainPoints.get(i + 1).x, mainPoints.get(i + 1).y,
                            1.0f
                    ));
                }

                for (int i = 0; i < numBranches; i++) {
                    int branchStartIndex = random.nextInt(mainPoints.size() - 1);
                    Point branchStart = mainPoints.get(branchStartIndex);
                    float branchEndX = branchStart.x + (random.nextFloat() - 0.5f) * 200;
                    float branchEndY = branchStart.y + random.nextFloat() * 200;

                    List<Point> branchPoints = generateBoltPoints(
                            branchStart.x, branchStart.y,
                            branchEndX, branchEndY,
                            0.2f
                    );

                    for (int j = 0; j < branchPoints.size() - 1; j++) {
                        segments.add(new LightningSegment(
                                branchPoints.get(j).x, branchPoints.get(j).y,
                                branchPoints.get(j + 1).x, branchPoints.get(j + 1).y,
                                0.5f
                        ));
                    }
                }
            }

            List<Point> generateBoltPoints(float x1, float y1, float x2, float y2, float jitter) {
                List<Point> points = new ArrayList<>();
                points.add(new Point(x1, y1));

                float dx = x2 - x1;
                float dy = y2 - y1;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                int numSegments = (int) (dist / 30); // Changed from 50 to 30 for more segments

                for (int i = 1; i < numSegments; i++) {
                    float t = i / (float) numSegments;
                    float x = x1 + dx * t + (random.nextFloat() - 0.5f) * dist * jitter * 0.7f; // Reduced jitter
                    float y = y1 + dy * t + (random.nextFloat() - 0.5f) * dist * jitter * 0.7f;
                    points.add(new Point(x, y));
                }

                points.add(new Point(x2, y2));
                return points;
            }

            boolean update() {
                long age = System.currentTimeMillis() - startTime;
                if (age > 500) return false;
                growthProgress = Math.min(1.0f, age / 100f);
                return true;
            }
        }

        public BatteryPuzzleView(Context context) {
            super(context);
            init();
            setupBatteryLevelListener();
            setupAnimations();
        }

        private void init() {
            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(WHITE);

            foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            foregroundPaint.setColor(RAZER_GREEN);

            foregroundGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            foregroundGlowPaint.setColor(RAZER_GREEN);
            foregroundGlowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.OUTER));

            thresholdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            thresholdPaint.setStyle(Paint.Style.STROKE);
            thresholdPaint.setStrokeWidth(4);
            thresholdPaint.setColor(THRESHOLD_GRAY);

            thresholdGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            thresholdGlowPaint.setStyle(Paint.Style.STROKE);
            thresholdGlowPaint.setStrokeWidth(4);
            thresholdGlowPaint.setColor(THRESHOLD_GRAY);
            thresholdGlowPaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));

            batteryRect = new RectF();
            batteryLevelReceiver = new BatteryLevelReceiver();
        }

        private class BatteryLevelReceiver {
            void updateBatteryLevel() {
                IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryIntent = getContext().registerReceiver(null, batteryIntentFilter);

                if (batteryIntent != null) {
                    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int newBatteryLevel = Math.round(level * 100f / scale);

                    if (newBatteryLevel != currentBatteryLevel) {
                        currentBatteryLevel = newBatteryLevel;
                        post(() -> invalidate());
                    }
                }
            }
        }

        private void setupBatteryLevelListener() {
            new Thread(() -> {
                while (!Thread.interrupted()) {
                    batteryLevelReceiver.updateBatteryLevel();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();
        }

        private void setupAnimations() {
            ValueAnimator sparkAnimator = ValueAnimator.ofFloat(0, 1);
            sparkAnimator.setDuration(16);
            sparkAnimator.setRepeatCount(ValueAnimator.INFINITE);
            sparkAnimator.setInterpolator(new LinearInterpolator());
            sparkAnimator.addUpdateListener(animation -> {
                thresholdSparkPosition += thresholdSparkVelocity;
                if (thresholdSparkPosition >= batteryRect.width() || thresholdSparkPosition <= 0) {
                    thresholdSparkVelocity = -thresholdSparkVelocity;
                }

                foregroundSparkPosition += foregroundSparkVelocity;
                if (foregroundSparkPosition >= batteryRect.width() || foregroundSparkPosition <= 0) {
                    foregroundSparkVelocity = -foregroundSparkVelocity;
                }

                // Check if it's time for a new lightning bolt
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLightningTime > 4000 + random.nextInt(3000)) {
                    generateNewLightning();
                    lastLightningTime = currentTime;
                }

                invalidate();
            });
            sparkAnimator.start();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            batteryRect.set(0, 0, w, h);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // Background
            canvas.drawRect(batteryRect, backgroundPaint);

            // Battery level
            float batteryHeight = batteryRect.height() * (currentBatteryLevel / 100f);
            float topY = batteryRect.bottom - batteryHeight;

            RectF filledRect = new RectF(
                    batteryRect.left,
                    topY,
                    batteryRect.right,
                    batteryRect.bottom
            );

            // Draw foreground with glow
            canvas.drawRect(filledRect, foregroundGlowPaint);
            canvas.drawRect(filledRect, foregroundPaint);

            drawBatteryHint(canvas);

            // Draw threshold line
            float thresholdY = batteryRect.height() * (thresholdLevel / 100f);
            canvas.drawLine(
                    batteryRect.left,
                    batteryRect.bottom - thresholdY,
                    batteryRect.right,
                    batteryRect.bottom - thresholdY,
                    thresholdPaint
            );

            // Draw threshold spark
            drawSpark(canvas, thresholdSparkPosition, batteryRect.bottom - thresholdY, THRESHOLD_GRAY);

            // Draw top edge line
            canvas.drawLine(batteryRect.left, topY, batteryRect.right, topY, thresholdPaint);



            // Draw lightning bolts
            Iterator<LightningBolt> iterator = lightningBolts.iterator();
            while (iterator.hasNext()) {
                LightningBolt bolt = iterator.next();
                if (!bolt.update()) {
                    iterator.remove();
                } else {
                    drawLightning(canvas, bolt);
                }
            }

            // Level completion check
            if (currentBatteryLevel == thresholdLevel) {
                foregroundPaint.setColor(RAZER_GREEN);
                foregroundGlowPaint.setColor(RAZER_GREEN);
                GlobalVariables.getInstance().setLevelComplete(8);
                GlobalVariables.getInstance().saveData(getContext());
                // Draw foreground spark
                drawSpark(canvas, foregroundSparkPosition, topY, ELECTRIC_BLUE);
            } else {
                foregroundPaint.setColor(ELECTRIC_YELLOW);
                foregroundGlowPaint.setColor(ELECTRIC_YELLOW);
                // Draw foreground spark
                drawSpark(canvas, foregroundSparkPosition, topY, ELECTRIC_YELLOW);
            }
        }

        private void drawSpark(Canvas canvas, float x, float y, int color) {
            Paint sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sparkPaint.setColor(color);
            sparkPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));

            canvas.drawCircle(x, y, SPARK_SIZE/2, sparkPaint);

            sparkPaint.setAlpha(100);
            canvas.drawCircle(x, y, SPARK_SIZE, sparkPaint);
        }

        private void generateNewLightning() {
            int side = random.nextInt(4);
            float startX = 0, startY = 0, endX, endY;

            switch (side) {
                case 0: // Top
                    startX = random.nextFloat() * batteryRect.width();
                    startY = 0;
                    endX = startX + (random.nextFloat() - 0.5f) * 200;
                    endY = random.nextFloat() * batteryRect.height() * 0.7f;
                    break;
                case 1: // Right
                    startX = batteryRect.width();
                    startY = random.nextFloat() * batteryRect.height();
                    endX = startX - random.nextFloat() * batteryRect.width() * 0.7f;
                    endY = startY + (random.nextFloat() - 0.5f) * 200;
                    break;
                case 2: // Left
                    startX = 0;
                    startY = random.nextFloat() * batteryRect.height();
                    endX = random.nextFloat() * batteryRect.width() * 0.7f;
                    endY = startY + (random.nextFloat() - 0.5f) * 200;
                    break;
                default: // Bottom
                    startX = random.nextFloat() * batteryRect.width();
                    startY = batteryRect.height();
                    endX = startX + (random.nextFloat() - 0.5f) * 200;
                    endY = batteryRect.height() - random.nextFloat() * batteryRect.height() * 0.7f;
                    break;
            }

            lightningBolts.add(new LightningBolt(startX, startY, endX, endY));
        }

        private void drawLightning(Canvas canvas, LightningBolt bolt) {
            Paint lightningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            lightningPaint.setColor(ELECTRIC_BLUE); // Changed to blue
            lightningPaint.setStyle(Paint.Style.STROKE);
            lightningPaint.setStrokeWidth(3);

            Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glowPaint.setColor(DEEP_BLACK); // Changed to blue
            glowPaint.setStyle(Paint.Style.STROKE);
            glowPaint.setStrokeWidth(10);
            glowPaint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.NORMAL));

            int segmentsToShow = (int) (bolt.segments.size() * bolt.growthProgress);
            for (int i = 0; i < segmentsToShow; i++) {
                LightningSegment segment = bolt.segments.get(i);

                // Draw glow
                glowPaint.setAlpha((int) (128 * segment.brightness));
                canvas.drawLine(segment.x1, segment.y1, segment.x2, segment.y2, glowPaint);

                // Draw core
                lightningPaint.setAlpha((int) (255 * segment.brightness));
                canvas.drawLine(segment.x1, segment.y1, segment.x2, segment.y2, lightningPaint);
            }
        }

        private void drawBatteryHint(Canvas canvas) {
            float boxWidth = 50;
            float boxHeight = 50;
            float padding = 10;
            float cornerRadius = 8; // For rounded corners

            Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            hintPaint.setColor(ELECTRIC_YELLOW);
            hintPaint.setStyle(Paint.Style.FILL);

            // Create glow effect
            Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glowPaint.setColor(ELECTRIC_YELLOW);
            glowPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.OUTER));

            RectF hintRect = new RectF(
                    batteryRect.right - padding - boxWidth - 10,
                    padding,
                    batteryRect.right - padding - 13,
                    padding + boxHeight
            );



            // Draw glow
            canvas.drawRoundRect(hintRect, cornerRadius, cornerRadius, glowPaint);
            // Draw box
            canvas.drawRoundRect(hintRect, cornerRadius, cornerRadius, hintPaint);
        }
    }
}