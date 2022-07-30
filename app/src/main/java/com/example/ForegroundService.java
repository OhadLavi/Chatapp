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

    public static final String CHANNEL_ID_1 = "NotificationChannel";
    private FirebaseAuth auth;
    private Notification notification1; //TODO: delete notification1
    private Context context;
    private long numberOfUsers;
    private boolean flag = true;

    @Override
    public void onCreate() {
        super.onCreate();
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
        // When User clicks on new media notification , moves to Notification_reciever.
        Intent notificationIntent = new Intent(this, NotificationServiceHandler.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        createNotificationChannel_2();
        auth = FirebaseAuth.getInstance();
        context = this;
        notification1 = new NotificationCompat.Builder(context, CHANNEL_ID_1)
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
                    startForeground(1, notification1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Called from notification's receiver
        if (intent != null && intent.getAction() != null && intent.getAction().equals("STOP_ACTION"))
            startForeground(1, notification1); //Clicking on the notification will restarts the service with "STOP_ACTION"
        return START_STICKY;
    }

    private void createNotificationChannel_2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_1,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            stopForeground(false);
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