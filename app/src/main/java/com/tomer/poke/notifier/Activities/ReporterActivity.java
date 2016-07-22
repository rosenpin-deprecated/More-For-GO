package com.tomer.poke.notifier.Activities;

import android.os.Bundle;

import com.heinrichreimersoftware.androidissuereporter.IssueReporterActivity;
import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo;
import com.heinrichreimersoftware.androidissuereporter.model.github.GithubTarget;
import com.tomer.poke.notifier.SecretConstants;

public class ReporterActivity extends IssueReporterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setGuestEmailRequired(true);
    }

    @Override
    public GithubTarget getTarget() {
        return new GithubTarget("rosenpin", "notifications-for-pokemon-go");
    }

    @Override
    public String getGuestToken() {
        return SecretConstants.getPropertyValue(this, "github-key");
    }

    @Override
    public void onSaveExtraInfo(ExtraInfo extraInfo) {

    }
}
