package com.tomer.poke.notifier.plus.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import com.heinrichreimersoftware.androidissuereporter.IssueReporterActivity;
import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo;
import com.heinrichreimersoftware.androidissuereporter.model.github.GithubTarget;
import com.tomer.poke.notifier.plus.Globals;
import com.tomer.poke.notifier.plus.Prefs;
import com.tomer.poke.notifier.plus.SecretConstants;

public class ReporterActivity extends IssueReporterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setGuestEmailRequired(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toolbar toolbar = (Toolbar) findViewById(com.heinrichreimersoftware.androidissuereporter.R.id.air_toolbar);
            toolbar.setBackgroundColor(Globals.toolbarColor);
            toolbar.setTitleTextColor(Color.WHITE);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Globals.toolbarColorDark);
        }
    }

    @Override
    public GithubTarget getTarget() {
        return new GithubTarget("rosenpin", "Enhancer-For-GO");
    }

    @Override
    public String getGuestToken() {
        return SecretConstants.getPropertyValue(this, "github-key");
    }

    @Override
    public void onSaveExtraInfo(ExtraInfo extraInfo) {
        Prefs prefs = new Prefs(this);
        String[][] preferences = prefs.toArray();
        for (String[] preference : preferences) {
            extraInfo.put(preference[0], preference[1]);
        }
    }
}
