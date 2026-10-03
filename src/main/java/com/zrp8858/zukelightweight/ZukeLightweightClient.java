package com.zrp8858.zukelightweight;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zukeLightweight: mostly-vanilla quality-of-life features, entirely client
 * side. Features are added one at a time; each registers its own hooks from
 * here.
 */
public class ZukeLightweightClient implements ClientModInitializer {
    public static final String MOD_ID = "zuke-lightweight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("zukeLightweight loaded");
    }
}
