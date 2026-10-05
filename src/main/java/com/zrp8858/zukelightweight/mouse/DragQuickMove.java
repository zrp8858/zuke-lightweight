package com.zrp8858.zukelightweight.mouse;

import com.mojang.blaze3d.platform.InputConstants;
import com.zrp8858.zukelightweight.config.Configs;
import com.zrp8858.zukelightweight.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;


/**
 * Shift + left-click drag: hold Shift, press the left button on a stack, and
 * drag across more stacks -- each one is quick-moved (shift-clicked) once.
 * Vanilla already handles the first click; this handles the slots you drag
 * across afterwards.
 */
final class DragQuickMove {
    /** Max distance between samples along a fast mouse move, so no slot is skipped. */
    private static final double SAMPLE_STEP = 4.0;
    /**
     * The slot the cursor was over at the last sample, or null between slots. An item
     * is moved when the cursor *enters* a slot, so leaving and re-entering a slot moves
     * what's in it again. Compared by identity, not {@code Slot.index}: the creative
     * inventory's wrapper slots all report index 0.
     */
    private Slot currentSlot;
    private boolean active;
    private double lastX;
    private double lastY;

    void onClick(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        currentSlot = null;
        active = event.button() == InputConstants.MOUSE_BUTTON_LEFT
                && Minecraft.getInstance().hasShiftDown()
                && Configs.MouseTweaks.DRAG_QUICK_MOVE.getBooleanValue()
                && screen.getMenu().getCarried().isEmpty();
        if (!active) {
            return;
        }
        lastX = event.x();
        lastY = event.y();
        currentSlot = slotAt(screen, lastX, lastY); // vanilla's own shift-click already moved it
    }

    void onDrag(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        if (!active || event.button() != InputConstants.MOUSE_BUTTON_LEFT || !Minecraft.getInstance().hasShiftDown()) {
            return;
        }

        double dx = event.x() - lastX;
        double dy = event.y() - lastY;
        int steps = Math.max(1, (int) Math.ceil(Math.hypot(dx, dy) / SAMPLE_STEP));
        for (int i = 1; i <= steps; i++) {
            quickMoveAt(screen, lastX + dx * i / steps, lastY + dy * i / steps);
        }
        lastX = event.x();
        lastY = event.y();
    }

    void onRelease() {
        active = false;
        currentSlot = null;
    }

    private void quickMoveAt(AbstractContainerScreen<?> screen, double x, double y) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Slot slot = slotAt(screen, x, y);
        if (slot == currentSlot) {
            return; // still inside the slot we already acted on
        }
        currentSlot = slot;
        // Skip crafting-result slots: dragging across one shouldn't craft.
        if (player == null || slot == null || slot instanceof ResultSlot || !slot.hasItem()
                || isIgnored(screen, slot, player) || !slot.mayPickup(player)) {
            return;
        }
        // Button 0 + QUICK_MOVE is what a shift-left-click sends. Going through the
        // screen's own slotClicked keeps screens that override it (creative) working.
        accessor(screen).zukelightweight$slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
    }

    /** In the creative inventory only the player's own slots take part, not the item picker. */
    private static boolean isIgnored(AbstractContainerScreen<?> screen, Slot slot, Player player) {
        return screen instanceof CreativeModeInventoryScreen && slot.container != player.getInventory();
    }

    private static Slot slotAt(AbstractContainerScreen<?> screen, double x, double y) {
        return accessor(screen).zukelightweight$getHoveredSlot(x, y);
    }

    private static AbstractContainerScreenAccessor accessor(AbstractContainerScreen<?> screen) {
        return (AbstractContainerScreenAccessor) screen;
    }
}
