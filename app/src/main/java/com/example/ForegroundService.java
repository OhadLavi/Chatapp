package com.example;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.project3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForegroundService extends Service {

    private static final String CHANNEL_ID_1 = "ForegroundServiceChannel";
    private static final String CHANNEL_ID_2 ="NotificationChannel";
    private Notification notification1, notification2;
    private long numberOfUsers;
    private boolean isFirstLaunch = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel_1();
        createNotificationChannel_2();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users"); //get reference to users saved in FireBase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numberOfUsers = snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If user click on the notification, switch to the notification handler
        Intent notificationIntent = new Intent(this, NotificationServiceHandler.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        notification1 = new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setContentTitle("TextMe")
                .build();
        notification2 = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setContentTitle("TextMe")
                .setContentText("New user joined our app, you can start to chat together!")
                .setSmallIcon(R.drawable.ic_new_user)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle())
                .build();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (numberOfUsers < snapshot.getChildrenCount()) {
                    numberOfUsers = snapshot.getChildrenCount();
                    startForeground(2, notification2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (isFirstLaunch) {
            isFirstLaunch = false;
            startForeground(1, notification1);
        }

        // Called from notification's receiver
        if (intent != null && intent.getAction() != null && intent.getAction().equals("STOP_ACTION"))
            startForeground(1, notification1); //Clicking on the notification will restarts the service with "STOP_ACTION"
        return START_STICKY;
    }

    private void createNotificationChannel_1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_1,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createNotificationChannel_2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_2,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}