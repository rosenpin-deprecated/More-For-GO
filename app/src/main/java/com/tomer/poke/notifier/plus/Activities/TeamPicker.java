package com.tomer.poke.notifier.plus.Activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.tomer.poke.notifier.R;
import com.tomer.poke.notifier.plus.Prefs;


public class TeamPicker extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_picker);
        View[] cards = {findViewById(R.id.card_mystic), findViewById(R.id.card_valor), findViewById(R.id.card_instinct)};
        TextView[] textViews = {(TextView) findViewById(R.id.title_blue), (TextView) findViewById(R.id.title_red), (TextView) findViewById(R.id.title_yellow)};
        final Prefs prefs = new Prefs(this);
        for (int i = 0; i < cards.length; i++) {
            final int finalI = i;
            cards[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int theme = 0;
                    switch (finalI) {
                        case 0:
                            theme = 1;
                            break;
                        case 1:
                            theme = 2;
                            break;
                        case 2:
                            theme = 3;
                            break;
                    }
                    prefs.set(Prefs.setup, true);
                    prefs.set(Prefs.theme, theme);
                    startActivity(new Intent(TeamPicker.this, MainActivity.class));
                    finish();
                }
            });
        }
        findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.set(Prefs.setup, true);
                startActivity(new Intent(TeamPicker.this, MainActivity.class));
                finish();
            }
        });
        for (TextView textView : textViews) {
            textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/pokemon_font.ttf"));
        }
    }
}
