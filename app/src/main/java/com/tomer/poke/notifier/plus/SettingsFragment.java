package com.tomer.poke.notifier.plus;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import com.tomer.poke.notifier.R;
import com.tomer.poke.notifier.plus.Activities.MainActivity;

import java.io.IOException;
import java.util.List;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    boolean shouldAllowOverlay;
    boolean shouldAllowDim;
    private boolean shouldAllowMaximizeBrightness;
    private Intent mainServiceIntent;
    private boolean shouldAllowFab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.plus_settings);
        updateNotificationPreference();
        findPreference("battery_saver").setOnPreferenceChangeListener(this);
        findPreference("overlay").setOnPreferenceChangeListener(this);
        findPreference("dim").setOnPreferenceChangeListener(this);
        findPreference("extreme_battery_saver").setOnPreferenceChangeListener(this);
        findPreference("maximize_brightness").setOnPreferenceChangeListener(this);
        findPreference("show_fab").setOnPreferenceChangeListener(this);
        findPreference("persistent_notification").setOnPreferenceChangeListener(this);
        findPreference("translate").setOnPreferenceClickListener(this);
        mainServiceIntent = new Intent(getActivity(), MainService.class);
        restartService();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNotificationPreference();
        updatePermissionsBasedPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateNotificationPreference();
    }

    private void updatePermissionsBasedPreferences() {
        if (!hasDrawingPermission())
            ((TwoStatePreference) findPreference("overlay")).setChecked(false);
        else if (shouldAllowOverlay)
            ((TwoStatePreference) findPreference("overlay")).setChecked(true);

        if (!hasModifySettingsPermission())
            ((TwoStatePreference) findPreference("dim")).setChecked(false);
        else if (shouldAllowDim)
            ((TwoStatePreference) findPreference("dim")).setChecked(true);

        if (!hasDrawingPermission())
            ((TwoStatePreference) findPreference("show_fab")).setChecked(false);
        else if (shouldAllowFab)
            ((TwoStatePreference) findPreference("show_fab")).setChecked(true);

        if (!hasModifySettingsPermission())
            ((TwoStatePreference) findPreference("maximize_brightness")).setChecked(false);
        else if (shouldAllowMaximizeBrightness)
            ((TwoStatePreference) findPreference("maximize_brightness")).setChecked(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findPreference("screen_of_proximity").setEnabled(false);
            ((TwoStatePreference) findPreference("screen_of_proximity")).setChecked(false);
        }
        if (!hasModifySecurePermission())
            ((TwoStatePreference) findPreference("extreme_battery_saver")).setChecked(false);
    }

    private void updateNotificationPreference() {
        findPreference("notification").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), com.tomer.poke.notifier.Activities.MainActivity.class));
                return false;
            }
        });
    }

    private void noSecureSettingsPermissionPrompt() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.root_workaround))
                .setMessage(getString(R.string.root_workaround_desc))
                .setPositiveButton(R.string.share_command, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.plus_adb_command));
                        startActivity(Intent.createChooser(share, getString(R.string.share_command_title)));
                    }
                })
                .setNegativeButton(getString(R.string.how_to), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContextUtils.openUrl(getActivity(), "http://lifehacker.com/the-easiest-way-to-install-androids-adb-and-fastboot-to-1586992378");
                    }
                })
                .setNeutralButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private boolean isNotificationForGOInstalled() {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getActivity().getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.tomer.poke.notifier"))
                return true;
        }
        return false;
    }

    private boolean hasDrawingPermission() {
        try {
            View view = new View(getActivity());
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).addView(view, lp);
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).removeView(view);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasModifySettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getActivity())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasModifySecurePermission() {
        try {
            int originalLocationMode = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
            Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
            Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE, originalLocationMode);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void restartService() {
        getActivity().stopService(mainServiceIntent);
        getActivity().startService(mainServiceIntent);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals("battery_saver")) {
            if (!hasModifySecurePermission()) {
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + getActivity().getPackageName() + " android.permission.WRITE_SECURE_SETTINGS"});
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.warning_1_root, Snackbar.LENGTH_LONG).setAction(R.string.root_workaround, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            noSecureSettingsPermissionPrompt();
                        }
                    }).show();
                    return false;
                }
            }
        }
        if (preference.getKey().equals("overlay")) {
            if (!hasDrawingPermission()) {
                MainActivity.askForPermission(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            shouldAllowOverlay = true;
                        }
                    }
                }, false, "show a black screen over other apps");
                return false;
            }
        }
        if (preference.getKey().equals("show_fab")) {
            if (!hasDrawingPermission()) {
                MainActivity.askForPermission(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            shouldAllowFab = true;
                        }
                    }
                }, false, "show a black screen over other apps");
                return false;
            }
        }
        if (preference.getKey().equals("dim")) {
            if (!hasModifySettingsPermission()) {
                MainActivity.askForPermission(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            shouldAllowDim = true;
                        }
                    }
                }, false, "change system settings");
                return false;
            }
        }
        if (preference.getKey().equals("extreme_battery_saver")) {
            if (!hasModifySecurePermission()) {
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + getActivity().getPackageName() + " android.permission.WRITE_SECURE_SETTINGS"});
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.warning_1_root, Snackbar.LENGTH_LONG).setAction(R.string.root_workaround, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            noSecureSettingsPermissionPrompt();
                        }
                    }).show();
                    return false;
                }
            }
            if ((Globals.ownedItems == null || Globals.ownedItems.size() == 0) && (Boolean) o) {
                MainActivity.promptSupport(getActivity());
                return false;
            }
        }
        if (preference.getKey().equals("maximize_brightness")) {
            if (!hasModifySettingsPermission()) {
                MainActivity.askForPermission(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            shouldAllowMaximizeBrightness = true;
                        }
                    }
                }, false, "change system settings");
                return false;
            }
        }
        if (preference.getKey().equals("persistent_notification")) {
            if (!(boolean) o) {
                NotificationManager notificationManger = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManger.cancel(33);
            }
        }
        restartService();
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("translate"))
            ContextUtils.openUrl(getActivity().getApplicationContext(), "https://crowdin.com/project/enhancements-for-go");
        return false;
    }
}
