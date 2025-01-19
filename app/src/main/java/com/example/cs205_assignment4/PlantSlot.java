package com.example.cs205_assignment4;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.Log;

public class PlantSlot extends AppCompatImageView {
    // i tried to do dependency injection woohoo
    private DayNightService dayNightService;
    private EnergyService energyService;
    private int growthStage = 0; // growth state of plant -> 0: empty, 1: baby, 2: growing, 3: can harvest
    private final int GROWTH_TIME = 5000; // time taken to reach a new growth stage, change if needed
    private final Handler handler = new Handler();
    private Runnable growRunnable;
    private boolean isWaiting = false; // set to true if a plant's growth got halted

    public PlantSlot(Context context, DayNightService dayNightService, EnergyService energyService) {
        super(context);
        this.dayNightService = dayNightService;
        this.energyService = energyService;
        init();
    }

    public PlantSlot(Context context, DayNightService dayNightService, EnergyService energyService, AttributeSet attrs) {
        super(context, attrs);
        this.dayNightService = dayNightService;
        this.energyService = energyService;
        init();
    }

    public PlantSlot(Context context, DayNightService dayNightService, EnergyService energyService,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.dayNightService = dayNightService;
        this.energyService = energyService;
        init();
    }

    // creates an instance of a plant with its onClick events
    private void init() {
        Log.d(TAG, "Initializing PlantSlot with dirt image.");
        setImageResource(R.drawable.dirt); // a plant slot is empty by default
        setScaleType(ScaleType.FIT_CENTER);
        setOnClickListener(v -> {
            if(growthStage == 0) {
                grow(); // sow a plant on an empty slot
            } else if (growthStage == 3) {
                harvest(); // a fully grown plant can be harvested
            }
        });
    }

    private void grow() {
        boolean isNight = !dayNightService.isDay();
        boolean hasEnergy = energyService.getEnergyStored() > 0;
        // if a plant is currently growing and conditions are met, halt growth process
        if(growthStage != 0 && isNight && !hasEnergy) {
            isWaiting = true;
            checkAndResumeGrowth();
            return;
        }
        growthStage++;
        updatePlantImage();
        scheduleGrowth();
        isWaiting = false; // if a plant can grow, it is not halted
    }

    private void scheduleGrowth() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isNight = !dayNightService.isDay();
                boolean hasEnergy = energyService.getEnergyStored() > 0;
                if(isNight && !hasEnergy) {
                    isWaiting = true;
                    checkAndResumeGrowth();
                    return;
                }
                // advance growth stage only if conditions are met
                if (growthStage > 0 && growthStage < 3) {
                    growthStage++;
                    updatePlantImage();
                    if (growthStage < 3) {
                        scheduleGrowth();
                    }
                }
                isWaiting = false;
            }
        }, GROWTH_TIME);
    }

    // recurring check to make sure that plants resume growth when conditions are favorable
    private void checkAndResumeGrowth() {
        handler.postDelayed(() -> {
            boolean isDay = dayNightService.isDay();
            boolean hasEnergy = energyService.getEnergyStored() > 0;
            if (isDay || hasEnergy) {
                if (growthStage > 0 && growthStage < 3) {
                    grow();
                }
            } else {
                checkAndResumeGrowth();
            }
        }, 1000);
    }

    // harvests the (fully grown) plant and resets it back to an empty slot
    private void harvest() {
        growthStage = 0;
        updatePlantImage();
        handler.removeCallbacks(growRunnable);
        if (harvestListener != null) {
            harvestListener.onHarvest();
        }
    }

    private void updatePlantImage() {
        switch(growthStage) {
            case 0:
                this.setImageResource(R.drawable.dirt);
                break;
            case 1:
                this.setImageResource(R.drawable.crop_1);
                break;
            case 2:
                this.setImageResource(R.drawable.crop_2);
                break;
            case 3:
                this.setImageResource(R.drawable.crop_3);
                break;
            default:
                break;
        }
    }

    public interface OnHarvestListener {
        void onHarvest();
    }

    private OnHarvestListener harvestListener;

    public void setOnHarvestListener(OnHarvestListener harvestListener) {
        this.harvestListener = harvestListener;
    }
}
