package com.tomer.poke.notifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainService extends Service implements ContextConstant {

    SharedPreferences prefs;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(MAIN_SERVICE_LOG_TAG, "Service started");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Clear previous logs
        clearLog();
        //Check logs recursively
        checkLogs();
    }

    private boolean vibrationMatch(String log) {
        return (log.contains("vibrate") && log.contains("PackageName: com.nianticlabs.pokemongo")) || log.contains("com.nianticlabs.pokemongo, ms: 500");
    }

    private boolean mapUpdateMatch(String log) {
        return log.contains("Breadcrumb: UpdateMapPokemon : Adding wild pokemon:") && log.contains("Updating encounter");
    }

    private void checkLogs() {
        String log = readLogs();
        Log.i(MAIN_SERVICE_LOG_TAG, "Checking..");
        if (vibrationMatch(log) || mapUpdateMatch(log)) {
            log = readLogs();
            Log.d(MAIN_SERVICE_LOG_TAG, "New pokemon found");
            Log.d("Pokemon number", log);
            showNotification(vibrationMatch(log), mapUpdateMatch(log));
        }
        clearLog();

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        checkLogs();
                    }
                },
                5000);
    }

    private void showNotification(boolean vibration, boolean mapUpdate) {
        String message = "";
        message += vibration ? "Vibration" : null;
        message += mapUpdate ? "MapUpdate" : null;

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(message);
        builder.setOngoing(false);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setSmallIcon(R.drawable.ic_notification_on);
        Notification notification = builder.build();
        if (prefs.getBoolean("notification_sound", true)) {
            String alarms = prefs.getString("ringtone", "android.resource://" + getPackageName() + "/" + R.raw.notification_sound);
            notification.sound = Uri.parse(alarms);
        }
        NotificationManager notificationManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int id = 0;
        if (vibration) {
            id = 1;
        } else if (mapUpdate) {
            id = 2;
        }
        notificationManger.notify(id, notification);
    }

    public void clearLog() {
        try {
            new ProcessBuilder()
                    .command("logcat", "-c")
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLogs() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            return log.toString();
        } catch (IOException e) {
            return "";
        }
    }

}
