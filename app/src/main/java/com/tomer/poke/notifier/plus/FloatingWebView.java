package com.tomer.poke.notifier.plus;

import android.content.Context;
import android.view.KeyEvent;
import android.webkit.WebView;

public class FloatingWebView extends WebView {
    public FloatingWebView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Globals.windowManager.removeView(Globals.floatingWebView);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
