package com.tomer.poke.notifier.plus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ContextUtils {
    public static void openUrl(Context activity, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
