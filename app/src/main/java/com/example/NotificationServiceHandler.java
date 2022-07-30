package com.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class NotificationServiceHandler extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onReceive();
    }

    private void onReceive() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        this.startActivity(activityIntent);
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.setAction("STOP_ACTION");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}

