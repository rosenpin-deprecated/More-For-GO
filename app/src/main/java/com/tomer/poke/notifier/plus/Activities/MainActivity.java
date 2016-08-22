package com.tomer.poke.notifier.plus.Activities;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tomer.poke.notifier.R;
import com.tomer.poke.notifier.plus.ContextUtils;
import com.tomer.poke.notifier.plus.Globals;
import com.tomer.poke.notifier.plus.Prefs;
import com.tomer.poke.notifier.plus.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);
        if (!prefs.getBoolean(Prefs.setup, false)) {
            //Start setup
            startActivity(new Intent(this, TeamPicker.class));
            finish();
        } else {
            //Actual oncreate
            applyTheme();
            setContentView(R.layout.plus_activity_main);
            getFragmentManager().beginTransaction()
                    .replace(R.id.preferences_holder, new SettingsFragment())
                    .commit();
        }
    }

    private void applyTheme() {
        try {
            int theme = prefs.getInt(Prefs.theme, 0);
            if (theme <= 3 && theme >= 0)
                setTheme(theme == 1 ? R.style.MysticTheme : (theme == 2 ? R.style.ValorTheme : (theme == 3 ? R.style.InstinctTheme : R.style.AppTheme)));
            else
                throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(new Intent(this, TeamPicker.class));
            finish();
        }
    }

    private boolean hasUsageAccess() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static void askForPermission(Activity activity, DialogInterface.OnClickListener onClickListener, boolean app, String permissionName) {
        new AlertDialog.Builder(activity)
                .setTitle("A permission is required")
                .setMessage("This " + (app ? "app " : "feature ") + "requires a special permission to " + permissionName + ", click grant to allow it now")
                .setPositiveButton("Grant permission", onClickListener)
                .show();
    }

    private void handleUsageAccessPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                if (!hasUsageAccess()) {
                    askForPermission(this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    }, true, "access usage");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                askForPermission(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivity(intent);
                    }
                }, true, "access usage");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.plus_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_change_team:
                startActivity(new Intent(getApplicationContext(), TeamPicker.class));
                finish();
                break;
            case R.id.menu_feedback:
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                Globals.toolbarColor = typedValue.data;
                getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
                Globals.toolbarColorDark = typedValue.data;
                startActivity(new Intent(getApplicationContext(), ReporterActivity.class));
                break;
            case R.id.menu_about:
                ContextUtils.openUrl(this, "https://github.com/rosenpin/Enhancer-For-GO");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleUsageAccessPermission();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            Log.d("Purchase state", String.valueOf(resultCode));
            if (resultCode == RESULT_OK) {
                Log.d(MainActivity.class.getSimpleName(), "Purchase");
                Toast.makeText(this, R.string.thank_you, Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                finish();
            }
        }
    }
}
