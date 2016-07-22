package com.tomer.poke.notifier.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.tomer.poke.notifier.BuildConfig;
import com.tomer.poke.notifier.ContextConstant;
import com.tomer.poke.notifier.Globals;
import com.tomer.poke.notifier.R;

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
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Create notification to stop the service
        createCancelNotification();
        //Clear previous logs
        clearLog();
        //Check logs recursively
        checkLogs();
        //Update the global status
        Globals.active = true;
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
            Log.i(MAIN_ACTIVITY_LOG_TAG, "FOUND!");
            log = readLogs();
            showNotification(log, vibrationMatch(log), mapUpdateMatch(log));
        }
        clearLog();

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        System.gc();
                        checkLogs();
                    }
                },
                5000);
    }

    private void showNotification(String log, boolean vibration, boolean mapUpdate) {
        String message = "";
        if (BuildConfig.DEBUG) {
            message += vibration ? "Vibration" : "";
            message += mapUpdate ? "MapUpdate" : "";
        }
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name));

        String found;
        try {
            int position = Integer.parseInt((log.substring(log.indexOf("Breadcrumb: UpdateMapPokemon : Adding wild pokemon:") + "Breadcrumb: UpdateMapPokemon : Adding wild pokemon:".length(), log.indexOf("Breadcrumb: UpdateMapPokemon : Adding wild pokemon:") + "Breadcrumb: UpdateMapPokemon : Adding wild pokemon:".length() + 4)).replaceAll(" ", ""));
            position--;
            if (position > 100) {
                position = position / 10;
            }
            found = pokemon[position];
        } catch (Exception e) {
            found = "Unknown Pokemon nearby";
        }
        builder.setContentText(message + "A " + found + " is nearby");
        builder.setOngoing(false);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setSmallIcon(R.drawable.ic_notification_on);
        Notification notification = builder.build();
        if (prefs.getBoolean("notification_sound", true)) {
            String sound = prefs.getString("notification_sound_file", "android.resource://" + getPackageName() + "/" + R.raw.notification_sound);
            notification.sound = Uri.parse(sound);
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

    private void createCancelNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("Stop the service here");
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setSmallIcon(R.drawable.ic_notification_on);
        Notification notification = builder.build();
        notification.contentIntent = PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), stopServiceService.class), 0);
        NotificationManager notificationManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManger.notify(11, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Remove the notification
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(11);
        //Update the global status
        Globals.active = false;
    }
}
