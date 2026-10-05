package com.zrp8858.zukelightweight.config;

import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;

/** Registers our hotkeys with malilib so they're live in-game and listed in its hotkey screens. */
public final class InputHandler implements IKeybindProvider {
    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (var hotkey : Configs.ALL_HOTKEYS) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory("zukeLightweight", "zuke-lightweight.hotkeys.category", Configs.ALL_HOTKEYS);
    }
}
