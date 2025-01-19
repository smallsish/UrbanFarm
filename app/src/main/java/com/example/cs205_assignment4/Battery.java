package com.example.cs205_assignment4;

import java.util.ArrayList;
import java.util.List;

public class Battery implements EnergyService {
    private final double capacity; // Capacity of the battery in kWh
    private double chargeLevel; // Current charge level of the battery in kWh
    private final double drainRate = 5;
    private final SunMoon sunmoon;
    private final List<BatteryListener> listeners;

    public Battery(double capacity, SunMoon sunmoon) {
        this.capacity = capacity;
        this.chargeLevel = 0; // Battery starts with zero charge
        this.sunmoon = sunmoon;
        this.listeners = new ArrayList<>();
        useBattery();
    }

    public void addListener(BatteryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BatteryListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (BatteryListener listener : listeners) {
            listener.onBatteryLevelChanged(chargeLevel / capacity);
        }
    }

    public void useBattery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(sunmoon.getTimeFactor() / 10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!sunmoon.isDay()) {
                        drain(drainRate);
                    }
                }
            }
        }).start();
    }

    public synchronized void charge(double energy) {
        // Add the collected energy to the battery's charge level
        chargeLevel = Math.min(chargeLevel + energy, capacity);
        notifyListeners(); // Notify listeners of the change
    }

    public synchronized void drain(double energy) {
        // Add the collected energy to the battery's charge level
        chargeLevel = Math.max(chargeLevel - energy, 0);
        notifyListeners(); // Notify listeners of the change
    }

    public double getCapacity() {
        return capacity;
    }

    // Method to retrieve the stored energy level
    public double getEnergyStored() {
        return chargeLevel;
    }

    public interface BatteryListener {
        void onBatteryLevelChanged(double level);
    }
}
