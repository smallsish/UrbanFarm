package com.example.cs205_assignment4;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        adjustWindowSize();
    }

    private void adjustWindowSize() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
        getWindow().setAttributes(params);
    }

    // Method to close the Help Activity
    public void closeHelpActivity(View view) {
        finish(); // Closes the current activity
    }
}
