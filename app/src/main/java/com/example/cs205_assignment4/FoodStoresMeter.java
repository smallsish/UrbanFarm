package com.example.cs205_assignment4;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FoodStoresMeter {
    private final ProgressBar foodStoresMeter;
    private LivelihoodMeter livelihoodMeter;
    private TextView foodStoresView;
    private final Handler handler;
    private final Context context;
    private int foodStores = 30; // start with some food stores
    private final int MAX_CAPACITY = 200; // maximum capacity
    private final int CONSUMPTION_RATE = 1000; // how fast citizens consume stores and how quickly their livelihood depletes, in milliseconds
    private final Object lock = new Object(); // lock object for synchronization
    private boolean isConsuming = true;
    private boolean isDying = true;

    public FoodStoresMeter(Context context, Handler handler) {
        this.foodStoresMeter = ((Activity)context).findViewById(R.id.foodStoresMeter);
        this.context = context;
        this.handler = handler;
        updateDisplay();
        consumeFoodStores();
    }

    public void setLivelihoodMeter(LivelihoodMeter meter) {
        this.livelihoodMeter = meter;
    }

    public void increaseFoodStores(int amount) {
        synchronized(lock) {
            if(foodStores + amount <= MAX_CAPACITY) {
                foodStores += amount;
            } else {
                foodStores = MAX_CAPACITY;
            }
            handler.post(this::updateDisplay);
        }
    }

    private void updateDisplay() {
        int progress = (foodStores * 100) / MAX_CAPACITY;
        handler.post(() -> foodStoresMeter.setProgress(progress));
    }

    private void consumeFoodStores() {
        //oh noooo the citizens are consuming the food :o
        Thread consumptionThread = new Thread(() -> {
            while(isConsuming) {
                synchronized (lock) {
                    if(foodStores > 0) {
                        foodStores -= 2; // the citizens consume 2 food woop
                        // as the citizens consume, their livelihood restores
                        if(livelihoodMeter != null) {
                            livelihoodMeter.increaseLivelihood(1);
                            isDying = true;
                        }
                    } else { // if the food stores are empty, citizens "wait" for food which causes livelihood to decrease
                        // if there is no food left, the citizens will be angy
                        if (isDying) {
                            NotificationHelper notificationHelper = new NotificationHelper(context);
                            notificationHelper.showGameNotification();
                            isDying = false;
                        }
                        if(livelihoodMeter != null) {
                            livelihoodMeter.decreaseLivelihood(3);
                        }
                    }
                    handler.post(this::updateDisplay);
                    lock.notifyAll();
                }

                try {
                    Thread.sleep(CONSUMPTION_RATE);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        consumptionThread.start();
    }

    // call this function if needed
    public void stopConsumption() {
        isConsuming = false;
    }

    public void resumeConsumption() {
        isConsuming = true;
    }

    private void updateDisplayUI() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateDisplay();
            }
        });
    }
}
