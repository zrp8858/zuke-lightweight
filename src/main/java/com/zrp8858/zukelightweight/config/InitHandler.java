package com.zrp8858.zukelightweight.config;

import com.zrp8858.zukelightweight.ZukeLightweightClient;
import com.zrp8858.zukelightweight.gui.ConfigScreen;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;

/** Hooks the mod into malilib once malilib is ready. */
public final class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(ZukeLightweightClient.MOD_ID, new Configs());
        InputEventHandler.getKeybindManager().registerKeybindProvider(new InputHandler());

        // Name shown in malilib's mod-switcher dropdown (it would otherwise be
        // derived from the mod id, giving "Zuke-lightweight").
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(ZukeLightweightClient.MOD_ID, "zukeLightweight", ConfigScreen::new));

        Configs.Hotkeys.OPEN_CONFIG_GUI.getKeybind().setCallback((action, key) -> {
            GuiBase.openGui(new ConfigScreen());
            return true;
        });
    }
}
