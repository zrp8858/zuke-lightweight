package com.zrp8858.zukelightweight.mouse;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/** Mouse-driven inventory shortcuts. Each behavior is its own toggle in the config. */
public final class MouseTweaks {
    private MouseTweaks() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof AbstractContainerScreen<?> container)) {
                return;
            }
            // Fresh state per opened screen.
            DragQuickMove dragQuickMove = new DragQuickMove();
            ScreenMouseEvents.afterMouseClick(screen).register((s, event, consumed) -> {
                dragQuickMove.onClick(container, event);
                return consumed;
            });
            ScreenMouseEvents.afterMouseDrag(screen).register((s, event, dragX, dragY, consumed) -> {
                dragQuickMove.onDrag(container, event);
                return consumed;
            });
            ScreenMouseEvents.afterMouseRelease(screen).register((s, event, consumed) -> {
                dragQuickMove.onRelease();
                return consumed;
            });
        });
    }
}
