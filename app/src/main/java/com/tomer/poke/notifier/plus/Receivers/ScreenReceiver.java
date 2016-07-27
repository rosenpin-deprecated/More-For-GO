package com.tomer.poke.notifier.plus.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import com.tomer.poke.notifier.plus.Globals;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("Receiver","Received");
            try {
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).removeView(Globals.blackLayout);
            }catch (Exception ignored){
                Log.d("Receiver","View is not attached");
            }
        }
    }
}
