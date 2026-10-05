package com.zrp8858.zukelightweight;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.zrp8858.zukelightweight.gui.ConfigScreen;

/**
 * Optional Mod Menu integration: the "Configure" button in Mod Menu's mod list
 * opens the same malilib config screen as the in-game hotkey. Only ever
 * invoked by Mod Menu itself, so the mod doesn't need Mod Menu installed.
 */
public class ZukeLightweightModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigScreen screen = new ConfigScreen();
            screen.setParent(parent);
            return screen;
        };
    }
}
