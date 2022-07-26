package com.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.Fragments.GetNumber;
import com.example.project3.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private BroadcastReceiver myBroadcast = null;
    private static String firstInActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {  //if user already connected take him to the Dashboard (using explicit intent)
            Log.e("test2","2");
            Intent intent = new Intent(MainActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
        } else { //if user not connected load the get number fragment
            Log.e("test1", "1");
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment f = new GetNumber();
            ft.add(R.id.LoginContainer, f).addToBackStack("registerFragment").commit();
        }
        registerFlightMode();
    }

    private void registerFlightMode() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        //registerReceiver(myBroadcast = new FlightModeBroadcastReceiver(), filter);
    }

    public static class FlightModeBroadcastReceiver extends BroadcastReceiver {
        private static String network = "android.net.conn.CONNECTIVITY_CHANGE";
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
            NetworkInfo currentNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(firstInActivity == null)
                firstInActivity = "1";
            else
            if (currentNetworkInfo != null && currentNetworkInfo.isConnected())
                Toast.makeText(context, "Network ON", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "Network OFF", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        //unregisterReceiver(myBroadcast);
        super.onStop();
    }
}