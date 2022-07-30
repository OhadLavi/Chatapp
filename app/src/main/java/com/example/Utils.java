package com.example;

import static java.time.temporal.ChronoUnit.MINUTES;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static SimpleDateFormat sdf() {
        return new SimpleDateFormat("dd/MM/yyyy HH-mm-ss", Locale.FRENCH);
    }

    public static String getTimeAgo(long time) { //return a string with the "last seen" time
        if (time < 1000000000000L)
            time *= 1000;
        long now = System.currentTimeMillis();
        if (time > now || time <= 0)
            return null;
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static String getMessageDateTimeAgo(LocalDateTime localDateTime) { //return a string with the "last seen" time TODO: delete (?)
        Log.e("test", java.time.LocalDateTime.now().toString());
        if (Math.abs(MINUTES.between(java.time.LocalDateTime.now(), localDateTime)) > 60 * 24 * 2) {
            DateFormat format2 = new SimpleDateFormat("EEEE");
            String day = format2.format(localDateTime);
            return day;
        } else if (MINUTES.between(java.time.LocalDateTime.now(), localDateTime) > 60 * 24 * 2)
            return "yesterday";
        else
            return String.format(Locale.FRENCH, "%02d:%02d", localDateTime.getHour(), localDateTime.getMinute());
    }

    public void hideKeyBoard(Activity activity, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public String currentDate() { //return the date of today in the format sdf as defined below
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        sdf().setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        return sdf().format(date);
    }

    public boolean isStorageOk(Context context) { //return true if there is permission to read data from external storage
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void updateOnlineStatus(String online) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        HashMap<String, Object> map = new HashMap<>();
        map.put("online", online);
        databaseReference.updateChildren(map);
    }
}
