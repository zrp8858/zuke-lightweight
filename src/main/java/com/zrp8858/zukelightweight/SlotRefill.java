package com.zrp8858.zukelightweight;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/**
 * Slot refill: when the stack in your selected hotbar slot runs out (a block
 * placed, food eaten, a tool broken), a matching stack from the main
 * inventory is swapped into that slot. Purely client side -- it just sends the
 * same inventory click you could make by hand.
 */
public final class SlotRefill {
    private static final int HOTBAR_SIZE = 9;
    private static final int INVENTORY_SIZE = 36;

    /** What the selected slot held at the end of the previous tick. */
    private static int lastSlot = -1;
    private static ItemStack lastStack = ItemStack.EMPTY;

    private SlotRefill() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SlotRefill::tick);
    }

    private static void tick(Minecraft mc) {
        Player player = mc.player;
        if (player == null || mc.gameMode == null) {
            lastSlot = -1;
            lastStack = ItemStack.EMPTY;
            return;
        }

        Inventory inventory = player.getInventory();
        int slot = inventory.getSelectedSlot();
        ItemStack current = inventory.getItem(slot);

        // Only react when the same slot emptied between ticks, and not while a
        // screen is open (the player is rearranging the inventory themselves).
        boolean emptied = slot == lastSlot && current.isEmpty() && !lastStack.isEmpty();
        if (emptied && mc.gui.screen() == null) {
            int source = findReplacement(inventory, lastStack);
            if (source >= 0) {
                // Container 0 is the player's inventory menu: hotbar is menu
                // slots 36-44, main inventory is 9-35. SWAP's "button" is the
                // hotbar index to swap with.
                mc.gameMode.handleContainerInput(
                        player.inventoryMenu.containerId, source, slot, ContainerInput.SWAP, player);
            }
        }

        lastSlot = slot;
        lastStack = current.copy();
    }

    /** Menu slot of the first main-inventory stack matching {@code wanted}, or -1. */
    private static int findReplacement(Inventory inventory, ItemStack wanted) {
        for (int i = HOTBAR_SIZE; i < INVENTORY_SIZE; i++) {
            ItemStack candidate = inventory.getItem(i);
            if (!candidate.isEmpty() && ItemStack.isSameItemSameComponents(candidate, wanted)) {
                return i;
            }
        }
        return -1;
    }
}
