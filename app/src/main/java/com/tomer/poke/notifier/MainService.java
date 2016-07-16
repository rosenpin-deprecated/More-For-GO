package com.tomer.poke.notifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainService extends Service implements ContextConstant {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(MAIN_SERVICE_LOG_TAG, "Service started");
        //Clear previous logs
        clearLog();
        //Check logs recursively
        checkLogs();
    }

    private void checkLogs() {
        String log = readLogs();
        Log.i(MAIN_SERVICE_LOG_TAG, "Checking..");
        if (log.contains("UpdateMapPokemon : Adding wild pokemon:")) {
            log = readLogs();
            if (log.contains("vibrate") || log.contains("MapExploreState transitioned to child state WildPokemonEncounterState")) {
                Log.d(MAIN_SERVICE_LOG_TAG, "New pokemon found");
                Log.d("Pokemon number", log);
                showNotification();
            }
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

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("New pokemon!");
        builder.setOngoing(false);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setSmallIcon(R.drawable.ic_notification_on);
        Notification notification = builder.build();
        notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_sound);
        NotificationManager notificationManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManger.notify(1, notification);
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
