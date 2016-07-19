package com.tomer.poke.notifier.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class stopServiceService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Stop the service
        stopService(new Intent(getApplicationContext(), MainService.class));
        //Stop this service
        stopSelf();
    }
}
