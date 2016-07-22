package com.tomer.poke.notifier.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class ShortcutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainActivity.isPermissionGranted(this)) {
            MainActivity.startService(this);
        } else {
            Toast.makeText(ShortcutActivity.this, "Error, please open the notifications for GO app", Toast.LENGTH_SHORT).show();
        }
        MainActivity.startPokemonGO(this);
    }
}
