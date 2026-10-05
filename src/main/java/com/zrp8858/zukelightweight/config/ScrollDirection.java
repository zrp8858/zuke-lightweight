package com.zrp8858.zukelightweight.config;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

/** Which way the wheel pushes items, as a cycle button in the config. */
public enum ScrollDirection implements IConfigOptionListEntry {
    /** Scroll down pushes the item out of the slot; scroll up pulls one in. */
    NORMAL("normal"),
    /** The opposite of {@link #NORMAL}. */
    INVERTED("inverted"),
    /** Scrolling toward the other inventory on screen always pushes toward it. */
    POSITION_AWARE("position_aware"),
    /** The opposite of {@link #POSITION_AWARE}. */
    POSITION_AWARE_INVERTED("position_aware_inverted");

    private static final String TRANSLATION_PREFIX = "zuke-lightweight.config.scrollDirection.";

    private final String key;

    ScrollDirection(String key) {
        this.key = key;
    }

    public boolean isInverted() {
        return this == INVERTED || this == POSITION_AWARE_INVERTED;
    }

    public boolean isPositionAware() {
        return this == POSITION_AWARE || this == POSITION_AWARE_INVERTED;
    }

    @Override
    public String getStringValue() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return StringUtils.translate(TRANSLATION_PREFIX + key);
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        ScrollDirection[] values = values();
        int next = ordinal() + (forward ? 1 : -1);
        return values[Math.floorMod(next, values.length)];
    }

    @Override
    public IConfigOptionListEntry fromString(String name) {
        for (ScrollDirection direction : values()) {
            if (direction.key.equalsIgnoreCase(name)) {
                return direction;
            }
        }
        return NORMAL;
    }
}
