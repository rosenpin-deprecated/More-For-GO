package com.tomer.poke.notifier.plus.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;

import com.tomer.poke.notifier.R;
import com.tomer.poke.notifier.plus.Constants;
import com.tomer.poke.notifier.plus.Globals;


public class ChromeTabActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chrome_tabs);
        if (Globals.url != null) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();

            customTabsIntent.launchUrl(ChromeTabActivity.this, Uri.parse(Globals.url));
        } else {
            startActivity(getPackageManager().getLaunchIntentForPackage(Constants.GOPackageName));
            finish();
        }
    }

    boolean firstLaunch = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstLaunch) {
            startActivity(getPackageManager().getLaunchIntentForPackage(Constants.GOPackageName));
            finish();
        } else
            firstLaunch = false;
    }
}
