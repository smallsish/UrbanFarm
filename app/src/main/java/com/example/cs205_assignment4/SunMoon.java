package com.example.cs205_assignment4;

import android.os.Handler;
import android.os.Looper;

public class SunMoon implements DayNightService {
    private boolean isDay = true;
    private final Handler handler;
    private static final int TIME_FACTOR = 5000; // milliseconds per in-game hour
    private static final int TRANSITION_DURATION = TIME_FACTOR * 12; // 24 hours in a day
    private volatile boolean isRunning = true; // Flag to control the loop execution
    private float brightness;

    public SunMoon() {
        handler = new Handler(Looper.getMainLooper());
    }

    public interface DayNightListener {
        void onTransition(float brightness, boolean isDay, String timeOfDay);
    }
    private DayNightListener dayNightListener;

    public void setDayNightListener(DayNightListener listener) {
        this.dayNightListener = listener;
    }

    public void startDayNightCycle() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                while (isRunning) {
                    try {
                        Thread.sleep(100); // Update brightness every 100 milliseconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    float progress = (float) elapsedTime / TRANSITION_DURATION;

                    // Calculate angle based on time of day
                    double angle;
                    if (isDay) {
                        angle = Math.PI * progress; // Brightness increases until midday
                    } else {
                        angle = Math.PI * progress + Math.PI; // Brightness decreases until midnight
                    }

                    // Calculate brightness using sine function
                    brightness = (float) Math.sin(angle);

                    // Determine time of day
                    String timeOfDay = getTimeOfDay(elapsedTime);

                    // Post UI update on main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dayNightListener != null) {
                                dayNightListener.onTransition(brightness, isDay, timeOfDay);
                            }
                        }
                    });

                    if (elapsedTime >= TRANSITION_DURATION) {
                        isDay = !isDay;
                        startTime = System.currentTimeMillis();
                    }
                }
            }
        }).start();
    }

    // Method to stop the day-night cycle
    public void stopDayNightCycle() {
        isRunning = false; // Set the flag to false to stop the loop
    }

    public boolean isDay() {
        return isDay;
    }

    public int getTimeFactor() {
        return TIME_FACTOR;
    }

    public float getBrightness() {
        return brightness;
    }

    private String getTimeOfDay(long currentTimeMillis) {
        int hours;
        if (isDay) {
            hours = (int) ((currentTimeMillis / TIME_FACTOR) + 6) % 24; // Convert milliseconds to hours
        } else {
            hours = (int) ((currentTimeMillis / TIME_FACTOR) + 18) % 24; // Convert milliseconds to hours
        }
        return "" + hours;
    }
}