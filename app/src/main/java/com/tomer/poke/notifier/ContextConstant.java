package com.tomer.poke.notifier;

import com.tomer.poke.notifier.Activities.MainActivity;
import com.tomer.poke.notifier.Services.MainService;

public interface ContextConstant {
    String MAIN_ACTIVITY_LOG_TAG = MainActivity.class.getSimpleName();
    String MAIN_SERVICE_LOG_TAG = MainService.class.getSimpleName();
    String POKEMON_GO_PACKAGE_NAME = "com.nianticlabs.pokemongo";
}
