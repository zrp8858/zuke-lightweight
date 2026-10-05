package com.zrp8858.zukelightweight.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.zrp8858.zukelightweight.ZukeLightweightClient;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * All of zukeLightweight's settings, in malilib's style (the same one
 * Tweakeroo and MiniHUD use): every feature is a boolean toggle that can also
 * be bound to a hotkey. Saved to {@code config/zuke-lightweight.json}.
 */
public final class Configs implements IConfigHandler {
    private static final String CONFIG_FILE = ZukeLightweightClient.MOD_ID + ".json";

    /** Feature toggles (boolean + optional hotkey each). */
    public static final class Features {
        public static final ConfigBooleanHotkeyed SLOT_REFILL = new ConfigBooleanHotkeyed(
                "slotRefill", true, "",
                "zuke-lightweight.config.slotRefill.comment",
                "zuke-lightweight.config.slotRefill.name");

        public static final ConfigBooleanHotkeyed HOTBAR_SHUFFLE = new ConfigBooleanHotkeyed(
                "hotbarShuffle", false, "R",
                "zuke-lightweight.config.hotbarShuffle.comment",
                "zuke-lightweight.config.hotbarShuffle.name");

        public static final ImmutableList<ConfigBooleanHotkeyed> TOGGLES =
                ImmutableList.of(SLOT_REFILL, HOTBAR_SHUFFLE);
    }

    /** Mouse tweaks (boolean + optional hotkey each). */
    public static final class MouseTweaks {
        public static final ConfigBooleanHotkeyed DRAG_QUICK_MOVE = new ConfigBooleanHotkeyed(
                "dragQuickMove", true, "",
                "zuke-lightweight.config.dragQuickMove.comment",
                "zuke-lightweight.config.dragQuickMove.name");

        public static final ConfigBooleanHotkeyed DRAG_GATHER = new ConfigBooleanHotkeyed(
                "dragGather", true, "",
                "zuke-lightweight.config.dragGather.comment",
                "zuke-lightweight.config.dragGather.name");

        public static final ImmutableList<ConfigBooleanHotkeyed> TOGGLES =
                ImmutableList.of(DRAG_QUICK_MOVE, DRAG_GATHER);
    }

    /** Plain hotkeys that aren't feature toggles. */
    public static final class Hotkeys {
        public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey(
                "openConfigGui", "Z,C",
                "zuke-lightweight.config.openConfigGui.comment",
                "zuke-lightweight.config.openConfigGui.name");

        public static final ImmutableList<ConfigHotkey> HOTKEYS = ImmutableList.of(OPEN_CONFIG_GUI);
    }

    /** Every feature toggle across all tabs. */
    public static final List<ConfigBooleanHotkeyed> ALL_TOGGLES = ImmutableList.<ConfigBooleanHotkeyed>builder()
            .addAll(Features.TOGGLES)
            .addAll(MouseTweaks.TOGGLES)
            .build();

    /** Every hotkey-capable config, for registering with malilib's input handler. */
    public static final List<IHotkey> ALL_HOTKEYS;

    static {
        List<IHotkey> hotkeys = new ArrayList<>();
        hotkeys.addAll(Features.TOGGLES);
        hotkeys.addAll(MouseTweaks.TOGGLES);
        hotkeys.addAll(Hotkeys.HOTKEYS);
        ALL_HOTKEYS = List.copyOf(hotkeys);
    }

    @Override
    public void load() {
        Path file = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE);
        JsonUtils.loadFromFile(file, root -> {
            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                ConfigUtils.readConfigBase(obj, "Features", new ArrayList<IConfigBase>(Features.TOGGLES));
                ConfigUtils.readConfigBase(obj, "MouseTweaks", new ArrayList<IConfigBase>(MouseTweaks.TOGGLES));
                ConfigUtils.readConfigBase(obj, "Hotkeys", new ArrayList<IConfigBase>(Hotkeys.HOTKEYS));
            }
        });
    }

    @Override
    public void save() {
        JsonObject root = new JsonObject();
        ConfigUtils.writeConfigBase(root, "Features", new ArrayList<IConfigBase>(Features.TOGGLES));
        ConfigUtils.writeConfigBase(root, "MouseTweaks", new ArrayList<IConfigBase>(MouseTweaks.TOGGLES));
        ConfigUtils.writeConfigBase(root, "Hotkeys", new ArrayList<IConfigBase>(Hotkeys.HOTKEYS));
        JsonUtils.writeJsonToFile(root, FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE));
    }
}
