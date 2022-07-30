package com.example;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        this.getBaseContext().startService(serviceIntent);
        if (firebaseAuth.getCurrentUser() != null) {  //if user already connected take him to the Dashboard (using explicit intent)
            Intent intent = new Intent(MainActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
        } else { //if user not connected load the get number fragment
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