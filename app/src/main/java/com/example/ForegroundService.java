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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.Adapter.UserAdapter;
import com.example.MainActivity;
import com.example.Model.UserModel;
import com.example.project3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


public class ForegroundService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private ValueEventListener handler;
    Query docRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth;
    Notification notification;
    String message = null;
    Context context;
    PendingIntent pendingIntent;
    private long numberOfUsers;
    boolean initState = true;

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
        Toast.makeText(context, "0", Toast.LENGTH_SHORT).show();
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        auth = FirebaseAuth.getInstance();
        context=this;

        System.out.println("You became friend with " + "now you can share lists together!");
        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("New user")
                .setContentText("New user joined our app, you can start to chat together!")
                .setSmallIcon(R.drawable.back_arrow)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You became friends with: now you can share lists together!"))
                .build();
        //startForeground(1, notification);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users"); //get reference to users saved in FireBase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Toast.makeText(context, "1", Toast.LENGTH_SHORT).show();
                if(numberOfUsers != snapshot.getChildrenCount()){
                    Toast.makeText(context, "2", Toast.LENGTH_SHORT).show();
                    numberOfUsers = snapshot.getChildrenCount();
                    startForeground(1, notification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*docRef = db.collection("Users").document(auth.getCurrentUser().getEmail()).collection("Notifications");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        docRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("TAG", "Listen failed.", error);
                    return;
                }
                if (initState) {
                    initState = false;
                } else {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                System.out.println("You became friend with " + "now you can share lists together!");
                                notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setContentTitle("New friend")
                                        .setContentText("You became friends with: " + dc.getDocument().getData().get("name") + " now you can share lists together!")
                                        .setSmallIcon(R.drawable.back_arrow)
                                        .setContentIntent(pendingIntent)
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                                .bigText("You became friends with: " + dc.getDocument().getData().get("name") + " now you can share lists together!"))
                                        .build();
                                startForeground(1, notification);
                            default:
                                break;
                        }

                    }
                }
            }
        });*/

        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
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
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    public void startService(String value) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", value);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }
}