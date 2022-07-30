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
import androidx.core.content.ContextCompat;
import com.example.project3.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForegroundService extends Service {

    public static final String CHANNEL_ID_2 = "NotificationChannel";

    FirebaseAuth auth;
    Notification notification1, notification2; //TODO: delete notification1
    Context context;
    PendingIntent pendingIntent;
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
        notification2 = new NotificationCompat.Builder(context, CHANNEL_ID_2)
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

        // called from notification reciever
        if (intent.getAction() != null && intent.getAction().equals("STOP_ACTION")) {
            //When user clicks on new media notifcation, Notification_Reciever restarts the service with "STOP_ACTION".
            startForeground(2, notification2);
        }
        return START_STICKY;
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

    private void createNotificationChannel_2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_2,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            stopForeground(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void startService(String value) { //TODO: delete (?)
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", value);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() { //TODO: delete (?)
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }
}