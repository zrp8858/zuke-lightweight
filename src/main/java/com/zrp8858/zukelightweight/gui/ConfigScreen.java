package com.zrp8858.zukelightweight.gui;

import com.google.common.collect.ImmutableList;
import com.zrp8858.zukelightweight.ZukeLightweightClient;
import com.zrp8858.zukelightweight.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;

import java.util.List;

/**
 * The in-game config screen: a row of tabs across the top, one malilib config
 * list underneath. To add a tab, add an enum entry with the configs it shows.
 */
public final class ConfigScreen extends GuiConfigsBase {
    /** Remembered across openings so the screen reopens on the tab you left. */
    private static Tab currentTab = Tab.FEATURES;

    public ConfigScreen() {
        super(10, 50, ZukeLightweightClient.MOD_ID, null, "zuke-lightweight.gui.title");
    }

    @Override
    public void initGui() {
        super.initGui();
        clearOptions();

        int x = 10;
        for (Tab tab : Tab.values()) {
            String label = StringUtils.translate(tab.translationKey);
            ButtonGeneric button = new ButtonGeneric(x, 26, StringUtils.getStringWidth(label) + 10, 20, label);
            button.setEnabled(tab != currentTab);
            addButton(button, (b, mouseButton) -> {
                currentTab = tab;
                reCreateListWidget();
                getListWidget().resetScrollbarPosition();
                initGui();
            });
            x += button.getWidth() + 2;
        }
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        return ConfigOptionWrapper.createFor(currentTab.configs);
    }

    private enum Tab {
        FEATURES("zuke-lightweight.gui.tab.features", ImmutableList.copyOf(Configs.Features.TOGGLES)),
        MOUSE("zuke-lightweight.gui.tab.mouse", ImmutableList.copyOf(Configs.MouseTweaks.TOGGLES)),
        HOTKEYS("zuke-lightweight.gui.tab.hotkeys", ImmutableList.copyOf(Configs.Hotkeys.HOTKEYS));

        private final String translationKey;
        private final ImmutableList<IConfigBase> configs;

        Tab(String translationKey, ImmutableList<? extends IConfigBase> configs) {
            this.translationKey = translationKey;
            this.configs = ImmutableList.copyOf(configs);
        }
    }
}
