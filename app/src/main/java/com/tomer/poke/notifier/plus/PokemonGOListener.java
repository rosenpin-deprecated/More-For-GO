package com.tomer.poke.notifier.plus;

public interface PokemonGOListener {
    long REFRESH_INTERVAL = 1000;

    void onStart();

    void onStop();

    boolean isGoRunning();
}
