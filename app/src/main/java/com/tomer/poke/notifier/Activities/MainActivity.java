package com.tomer.poke.notifier.Activities;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.tomer.poke.notifier.ContextConstant;
import com.tomer.poke.notifier.R;
import com.tomer.poke.notifier.SecretConstants;
import com.tomer.poke.notifier.Services.MainService;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ContextConstant, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    Toolbar toolbar;
    private IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    public static boolean isPermissionGranted(Context c) {
        //Check if the app has permission to read system log
        String pname = c.getPackageName();
        String[] CMDLINE_GRANTPERMS = {"su", "-c", null};
        if (c.getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, pname) != 0) {
            try {
                CMDLINE_GRANTPERMS[2] = String.format("pm grant %s android.permission.READ_LOGS", pname);
                java.lang.Process p = Runtime.getRuntime().exec(CMDLINE_GRANTPERMS);
                int res = p.waitFor();
                Log.d(MAIN_ACTIVITY_LOG_TAG, "exec returned: " + res);
                if (res != 0)
                    throw new Exception("failed to become root");
                else
                    return true;
            } catch (Exception e) {
                Log.d(MAIN_ACTIVITY_LOG_TAG, "exec(): " + e);
                return false;
            }
        } else
            return true;
    }

    public static void startService(Context c) {
        //Start the listener service
        c.startService(new Intent(c.getApplicationContext(), MainService.class));
        //Show message
        Toast.makeText(c, "Service started", Toast.LENGTH_SHORT).show();
    }

    public static void killPokemonGO(Context c) {
        ((ActivityManager) c.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(POKEMON_GO_PACKAGE_NAME);
    }

    public static void startPokemonGO(Context c) {
        try {
            c.startActivity(c.getPackageManager().getLaunchIntentForPackage(POKEMON_GO_PACKAGE_NAME));
        } catch (Exception e) {
            Toast.makeText(c, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Set toolbar title + font
        TextView toolbarTV = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTV.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/pokemon_font.ttf"));
        toolbarTV.setTextColor(getResources().getColor(R.color.colorAccent));

        //Navigation Drawer
        navigationDrawer();

        //Set switch listener
        SwitchCompat masterSwitch = (SwitchCompat) findViewById(R.id.master_switch);
        masterSwitch.setOnCheckedChangeListener(this);

        //Set click listeners
        findViewById(R.id.create_shortcut).setOnClickListener(this);
        findViewById(R.id.support).setOnClickListener(this);

        //Set up IAP
        Intent billingServiceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        billingServiceIntent.setPackage("com.android.vending");
        bindService(billingServiceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void navigationDrawer() {
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.dark_background)
                .build();

        int textColor = ContextCompat.getColor(this, R.color.material_drawer_dark_primary_text);
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withSelectedItem(-1)
                .withSliderBackgroundColor(ContextCompat.getColor(this, R.color.material_drawer_dark_background))
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.settings).withTextColor(textColor),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.about).withTextColor(textColor)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case 1:
                                startActivity(new Intent(getApplicationContext(), Settings.class));
                                break;
                            case 2:
                                openUrl("https://github.com/rosenpin/notifications-for-pokemon-go");
                                break;
                        }

                        return true;
                    }
                })
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //The user donated a dollar
        try {
            //Try to consume the purchase so the user can donate again later
            mService.consumePurchase(3, getPackageName(), new JSONObject(data.getStringExtra("INAPP_PURCHASE_DATA")).getString("purchaseToken"));
        } catch (RemoteException | JSONException | RuntimeException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        //If trying to turn the service on
        if (b) {
            //Check If the app has permission to read system logs
            if (isPermissionGranted(this)) {
                //Update the UI
                ((TextView) findViewById(R.id.status)).setText(getString(R.string.status_active));
                //Start the service
                startService(this);
                //Start Pokemon GO
                startPokemonGO(this);
                return;
            }
            //Prompt and ask to enable the permission
            noPermissionPrompt();
            //Update the UI
            compoundButton.setChecked(false);
            ((TextView) findViewById(R.id.status)).setText(getString(R.string.status_inactive));
            return;
        }
        //Trying to turn the service off - stop the service
        stopService();
        //Update the UI
        ((TextView) findViewById(R.id.status)).setText(getString(R.string.status_inactive));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_shortcut:
                Intent shortcutIntent = new Intent(getApplicationContext(), ShortcutActivity.class);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Intent addIntent = new Intent();
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_label));
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
                addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                getApplicationContext().sendBroadcast(addIntent);
                Toast.makeText(MainActivity.this, "Shortcut created", Toast.LENGTH_SHORT).show();
                break;
            case R.id.support:
                try {
                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            SecretConstants.getPropertyValue(getApplicationContext(), "IAPID"), "inapp", SecretConstants.getPropertyValue(getApplicationContext(), "googleIAPCode"));
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                } catch (IntentSender.SendIntentException | RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(getApplicationContext(), ReporterActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void noPermissionPrompt() {
        //Show the alert dialog
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.required_step))
                .setMessage(getString(R.string.required_step_desc))
                .setPositiveButton(getString(R.string.root_workaround), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.root_workaround))
                                .setMessage(getString(R.string.root_workaround_desc))
                                .setPositiveButton(R.string.share_command, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("text/plain");
                                        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.adb_command));
                                        startActivity(Intent.createChooser(share, getString(R.string.share_command_title)));
                                    }
                                })
                                .setNegativeButton(getString(R.string.how_to), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        openUrl("http://lifehacker.com/the-easiest-way-to-install-androids-adb-and-fastboot-to-1586992378");
                                    }
                                })
                                .setNeutralButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                })
                .setNegativeButton("Root", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openUrl("http://www.xda-developers.com/root");
                    }
                })
                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

    }

    private void stopService() {
        //Stop the listener service
        stopService(new Intent(getApplicationContext(), MainService.class));
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
    }
}
