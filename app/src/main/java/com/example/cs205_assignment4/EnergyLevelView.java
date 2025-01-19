package com.example.cs205_assignment4;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class EnergyLevelView extends ProgressBar implements Battery.BatteryListener {
    private Paint paint;
    private double batteryCapacity = 100.0;
    private double energyLevel;

    public EnergyLevelView(Context context) {
        super(context);
    }

    public EnergyLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnergyLevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBattery(Battery battery) {
        battery.addListener(this); // Listen for changes in battery level
        batteryCapacity = battery.getCapacity();
        setMax((int) batteryCapacity);
        setProgress((int) battery.getEnergyStored());
    }

    @Override
    public void onBatteryLevelChanged(double level) {
        final int progress = (int) (level * batteryCapacity);
        post(() -> setProgress(progress));
    }
}
