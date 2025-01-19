package com.example.cs205_assignment4;

public class SolarPanel implements Runnable {
    private final SunMoon sunMoon;
    private static final int ENERGY_GENERATION_RATE = 5; // Adjust as needed

    private boolean isRunning = true;
    private final Battery battery;

    public SolarPanel(SunMoon sunMoon, Battery battery) {
        this.sunMoon = sunMoon;
        this.battery = battery;
    }


    @Override
    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(sunMoon.getTimeFactor() / 5); // Wait for the time factor (5 seconds)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (sunMoon.isDay()) {
                // Generate energy during the day
                battery.charge(ENERGY_GENERATION_RATE * sunMoon.getBrightness());
                //System.out.println("Energy stored: " + battery.getEnergyStored());
            }
        }
    }

    public void stop() {
        isRunning = false;
    }
}

