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
import net.minecraft.world.item.ItemStack;

/**
 * Left-button drag tweaks. They start when you press the left button on a slot while
 * your cursor is empty, and act on each slot the cursor then enters while the button
 * stays held. What happens depends on what's on the cursor and whether Shift is held:
 *
 * <ul>
 *   <li><b>Cursor empty, Shift held</b> (drag quick move): every stack you enter is
 *       quick-moved, like a shift-click.</li>
 *   <li><b>Holding a stack you just picked up, Shift not held</b> (drag gather): every
 *       stack of the same item you enter is added to the cursor stack, as long as it
 *       still fits.</li>
 *   <li><b>Holding a stack you just picked up, Shift held</b>: every stack of the same
 *       item you enter is quick-moved.</li>
 * </ul>
 *
 * Releasing the button is left entirely to vanilla.
 */
final class LeftDrag {
    /** Max distance between samples along a fast mouse move, so no slot is skipped. */
    private static final double SAMPLE_STEP = 4.0;

    /** Whether the cursor was empty just before the press (vanilla has since acted on it). */
    private boolean cursorEmptyBeforePress;
    private boolean active;
    /**
     * The slot the cursor was over at the last sample, or null between slots. A slot is
     * acted on when the cursor *enters* it, so leaving and re-entering moves its item
     * again. Compared by identity, not {@code Slot.index}: the creative inventory's
     * wrapper slots all report index 0.
     */
    private Slot currentSlot;
    private double lastX;
    private double lastY;

    /** Runs before vanilla handles the press. */
    void beforeClick(AbstractContainerScreen<?> screen) {
        cursorEmptyBeforePress = screen.getMenu().getCarried().isEmpty();
    }

    /** Runs after vanilla handled the press (it has picked up or shift-clicked by now). */
    void afterClick(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        // A press with something already on the cursor is vanilla's own drag-spread.
        active = event.button() == InputConstants.MOUSE_BUTTON_LEFT && cursorEmptyBeforePress;
        currentSlot = null;
        if (!active) {
            return;
        }
        lastX = event.x();
        lastY = event.y();
        currentSlot = slotAt(screen, lastX, lastY); // vanilla already acted on this one
    }

    void onDrag(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        if (!active || event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }

        double dx = event.x() - lastX;
        double dy = event.y() - lastY;
        int steps = Math.max(1, (int) Math.ceil(Math.hypot(dx, dy) / SAMPLE_STEP));
        for (int i = 1; i <= steps; i++) {
            enterSlotAt(screen, lastX + dx * i / steps, lastY + dy * i / steps);
        }
        lastX = event.x();
        lastY = event.y();
    }

    void onRelease() {
        active = false;
        currentSlot = null;
    }

    private void enterSlotAt(AbstractContainerScreen<?> screen, double x, double y) {
        Slot slot = slotAt(screen, x, y);
        if (slot == currentSlot) {
            return; // still inside the slot we already acted on
        }
        currentSlot = slot;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        // Result slots are skipped: dragging across one shouldn't craft.
        if (player == null || slot == null || slot instanceof ResultSlot || !slot.hasItem()
                || isIgnored(screen, slot, player) || !slot.mayPickup(player)) {
            return;
        }

        ItemStack cursor = screen.getMenu().getCarried();
        boolean shift = mc.hasShiftDown();
        if (cursor.isEmpty()) {
            if (shift && Configs.MouseTweaks.DRAG_QUICK_MOVE.getBooleanValue()) {
                quickMove(screen, slot);
            }
        } else if (Configs.MouseTweaks.DRAG_GATHER.getBooleanValue()
                && ItemStack.isSameItemSameComponents(cursor, slot.getItem())) {
            if (shift) {
                quickMove(screen, slot);
            } else {
                gather(screen, slot, cursor);
            }
        }
    }

    private static void quickMove(AbstractContainerScreen<?> screen, Slot slot) {
        // Button 0 + QUICK_MOVE is what a shift-left-click sends. Going through the
        // screen's own slotClicked keeps screens that override it (creative) working.
        click(screen, slot, ContainerInput.QUICK_MOVE);
    }

    /**
     * Adds the slot's whole stack to the cursor stack. Vanilla only merges the cursor into
     * the slot, so it takes two clicks: merge into the slot, then pick the result back up.
     * Skipped when it wouldn't all fit, since the leftovers would stay on the slot.
     */
    private static void gather(AbstractContainerScreen<?> screen, Slot slot, ItemStack cursor) {
        if (cursor.getCount() + slot.getItem().getCount() > cursor.getMaxStackSize() || !slot.mayPlace(cursor)) {
            return;
        }
        click(screen, slot, ContainerInput.PICKUP);
        click(screen, slot, ContainerInput.PICKUP);
    }

    private static void click(AbstractContainerScreen<?> screen, Slot slot, ContainerInput input) {
        accessor(screen).zukelightweight$slotClicked(slot, slot.index, 0, input);
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
