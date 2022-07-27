package com.example;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.project3.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID_1 = "ForegroundServiceChannel";
    public static final String CHANNEL_ID_2 = "NotificationChannel";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private DatabaseReference mSearchedLocationReference;
    private long numberOfUsers;

    private void createNotificationChannel1() {
        NotificationChannel serviceChannel1 = new NotificationChannel(
                CHANNEL_ID_1,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel1);
    }

    private void createNotificationChannel2() {
        NotificationChannel serviceChannel2 = new NotificationChannel(
                CHANNEL_ID_2,
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel2);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, Notification_Service_Handler.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification Foreground_notification = new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setContentTitle("Foreground Service")
                .setContentText("Service is listening to new users!")
                .setSmallIcon(R.drawable.back_arrow)
                .build();

        Notification New_user_notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setContentTitle("New User")
                .setContentText("New user registers to the app, chat with him now!")
                .setSmallIcon(R.drawable.back_arrow)
                .setContentIntent(pendingIntent)
                .build();

        databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (numberOfUsers != snapshot.getChildrenCount()) {
                    numberOfUsers = snapshot.getChildrenCount();
                    startForeground(2, New_user_notification); //When data changed, creates a new media notification.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        createNotificationChannel1();
        createNotificationChannel2();

        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

    class Notification_Service_Handler extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onReceive();
    }

    private void onReceive() {
        Intent activityIntent = new Intent(this, ChatActivity.class);
        this.startActivity(activityIntent);
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.setAction("STOP_ACTION");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}