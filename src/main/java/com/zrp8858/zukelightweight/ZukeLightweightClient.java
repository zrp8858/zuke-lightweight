package com.zrp8858.zukelightweight;

import com.zrp8858.zukelightweight.config.InitHandler;
import com.zrp8858.zukelightweight.mouse.MouseTweaks;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zukeLightweight: mostly-vanilla quality-of-life features, entirely client
 * side. Features are added one at a time; each is a toggle in {@code Configs}
 * and registers its own hooks from here.
 */
public class ZukeLightweightClient implements ClientModInitializer {
    public static final String MOD_ID = "zuke-lightweight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // malilib calls back once it's ready to take configs and hotkeys.
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
        SlotRefill.register();
        HotbarShuffle.register();
        MouseTweaks.register();
        LOGGER.info("zukeLightweight loaded");
    }
}
