package com.example.cs205_assignment4;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PlantSlot.OnHarvestListener {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private SunMoon sunMoon;
    private View dayNight;
    private EnergyLevelView energyLevelView;
    private Battery battery;
    private Handler handler;
    private Runnable runnable;
    private FoodStoresMeter foodStoresMeter;
    private LivelihoodMeter livelihoodMeter;
    private final int FOOD_AMOUNT = 15; // each harvest increases food stores by this amount
    private static final String CHANNEL_ID = "Channel ID1";
    Handler uiHandler = new Handler(Looper.getMainLooper());

    private List<SolarPanel> solarPanels;

    private NotificationHelper notificationHelper;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        hideStatusBar();

        setContentView(R.layout.activity_main);

        // Get the content view (root view) of the activity
        View contentView = findViewById(android.R.id.content);

        // Set an OnClickListener to the content view
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delay hiding the status bar by 1 second
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideStatusBar();
                    }
                }, 2000);
            }
        });

        foodStoresMeter = new FoodStoresMeter(this, uiHandler);
        livelihoodMeter = new LivelihoodMeter(this, uiHandler);
        foodStoresMeter.setLivelihoodMeter(livelihoodMeter);

        dayNight = findViewById(R.id.dayNight);
        Handler handler = new Handler(Looper.getMainLooper());

        // Start the day-night cycle simulation
        SunMoon sunMoon = new SunMoon();
        int battery_capacity = 250;
        battery = new Battery(battery_capacity, sunMoon);
        solarPanels = new ArrayList<>();
        ImageView skylineImage = findViewById(R.id.skylineImage);
        ImageView lampImage = findViewById(R.id.lampImage);

        int numberOfSolarPanels = 2;
        for (int i = 0; i < numberOfSolarPanels; i++) {
            SolarPanel solarPanel = new SolarPanel(sunMoon, battery);
            solarPanels.add(solarPanel);
            new Thread(solarPanel).start();
        }

        energyLevelView = findViewById(R.id.energyLevelView);
        energyLevelView.setBattery(battery);

        sunMoon.setDayNightListener(new SunMoon.DayNightListener() {
            // gets the time of day onTransition
            @Override
            public void onTransition(float brightness, boolean isDay, String timeOfDay) {
                // Update UI for smooth transition
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update squareView background color based on brightness
                        int backgroundColor = calculateBackgroundColor(brightness);
                        dayNight.setBackgroundColor(backgroundColor);
                        if (!isDay) {
                            if (battery.getEnergyStored() > 0) {
                                lampImage.setImageResource(R.drawable.lamp_on);
                            } else {
                                lampImage.setImageResource(R.drawable.lamp_off);
                            }
                            skylineImage.setImageResource(R.drawable.skyline_night);
                        } else {
                            lampImage.setImageResource(R.drawable.lamp_off);
                            skylineImage.setImageResource(R.drawable.skyline_day);
                        }

                        // Update and display time of day (Clock)
                        TextView timeOfDayTextView = findViewById(R.id.timeOfDayTextView);
                        timeOfDayTextView.setText(timeOfDay + ":00");
                    }
                });
            }
        });
        sunMoon.startDayNightCycle();

        // setting a grid for the planting slots
        GridLayout plantSlotsGridLayout = findViewById(R.id.plantSlotsGridLayout);
        int totalSlots = 8; // can change where needed
        int numColumns = plantSlotsGridLayout.getColumnCount();
        int numRows = totalSlots / numColumns;
        plantSlotsGridLayout.setRowCount(numRows);
        plantSlotsGridLayout.setColumnCount(numColumns);

        for (int i = 0; i < totalSlots; i++) {
            PlantSlot plantSlot = new PlantSlot(this, sunMoon, battery); // initializes a new planting slot
            plantSlot.setOnHarvestListener(this); // tracks user harvesting fully grown plants
            // creates the grid for plant slots
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            plantSlot.setLayoutParams(params);
            plantSlotsGridLayout.addView(plantSlot);
        }

//        // help button
//        Button btnHelp = findViewById(R.id.btnHelp);
//        btnHelp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
//                startActivity(helpIntent);
//            }
//        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar again when the activity resumes
        hideStatusBar();
    }

    private void hideStatusBar() {
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(WindowInsets.Type.statusBars());
        }
    }

    @Override
    protected void onDestroy() {

        // Remove the callback to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        Handler uiHandler = new Handler(Looper.getMainLooper());

        foodStoresMeter = new FoodStoresMeter(this, uiHandler);

        // Stop the day-night cycle and release associated resources
        if (sunMoon != null) {
            sunMoon.stopDayNightCycle();
        }

        // Stop all running solar panels and release associated resources
        if (solarPanels != null) {
            for (SolarPanel solarPanel : solarPanels) {
                solarPanel.stop();
            }
        }

        super.onDestroy();
        // Remove existing notifications from the status bar
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        notificationHelper.deleteNotificationChannel();
    }

    // method needed for OnHarvestListener interface created in PlantSlot class
    @Override
    public void onHarvest() {
        foodStoresMeter.increaseFoodStores(FOOD_AMOUNT);
    }

    private int calculateBackgroundColor(float brightness) {
        int dayColor = getResources().getColor(R.color.dayColor);
        int nightColor = getResources().getColor(R.color.nightColor);

        // Interpolate colors based on brightness
        int red = (int) (Color.red(dayColor) * brightness + Color.red(nightColor) * (1 - brightness));
        int green = (int) (Color.green(dayColor) * brightness + Color.green(nightColor) * (1 - brightness));
        int blue = (int) (Color.blue(dayColor) * brightness + Color.blue(nightColor) * (1 - brightness));

        // Ensure that RGB values stay within the valid range
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        // Return the interpolated color
        return Color.rgb(red, green, blue);
    }

    public void showGameOverScreen() {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("At the time your citizens needed you most, you vanished :(")
                .setPositiveButton("Try Again", (dialog, which) -> restartGame())
//                .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                .setNegativeButton("Go to Menu", (dialog, which) -> {
                    Intent intent = new Intent(this, MenuActivity.class);
                    //Clears task
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void restartGame() {
        Intent restartIntent = new Intent(this, IntroActivity.class);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(restartIntent);
    }
}
