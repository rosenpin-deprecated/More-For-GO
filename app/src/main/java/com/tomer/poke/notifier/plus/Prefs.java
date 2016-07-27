package com.tomer.poke.notifier.plus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

public class Prefs {
    public static String batterySaver = "battery_saver";
    public static String keepAwake = "keep_awake";
    public static String overlay = "overlay";
    public static String dim = "dim";
    public static String setup = "setup";
    public static String theme = "theme";
    public static String kill_background_processes = "kill_background_processes";
    public static String extreme_battery_saver = "extreme_battery_saver";
    public static String maximize_brightness = "maximize_brightness";
    public static String showFAB = "show_fab";
    public static String screen_of_proximity = "screen_of_proximity";
    public static String fab_position = "fab_position";
    public static String persistent_notification = "persistent_notification";
    private SharedPreferences preferences;

    public Prefs(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(key, def);
    }

    public int getInt(String key, int def) {
        return preferences.getInt(key, def);
    }

    public String getString(String key, String def) {
        return preferences.getString(key, def);
    }

    public void set(String key, boolean val) {
        preferences.edit().putBoolean(key, val).apply();
    }

    public void set(String key, int val) {
        preferences.edit().putInt(key, val).apply();
    }

    public void set(String key, String val) {
        preferences.edit().putString(key, val).apply();
    }

    public void apply(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String[][] toArray() {
        String[][] list = new String[preferences.getAll().size()][2];
        Map<String, ?> prefs = preferences.getAll();
        int i = 0;
        for (Map.Entry<String, ?> entry : prefs.entrySet()) {
            list[i][0] = entry.getKey();
            list[i][1] = entry.getValue().toString();
            i++;
        }
        return list;
    }
}
