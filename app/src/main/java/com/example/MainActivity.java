package com.example;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.Fragments.GetNumber;
import com.example.project3.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private static boolean isFirstLaunch = true;
    private static boolean newUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {  //if user already connected take him to the Dashboard (using explicit intent)
            if (isFirstLaunch) {
                isFirstLaunch = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    this.getBaseContext().startForegroundService(new Intent(this, ForegroundService.class));
                else
                    this.getBaseContext().startService(new Intent(this, ForegroundService.class));
            }
            Intent intent = new Intent(MainActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
        } else { //if user not connected load the get number fragment
            newUser = true;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment f = new GetNumber();
            ft.add(R.id.LoginContainer, f).addToBackStack("registerFragment").commit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}