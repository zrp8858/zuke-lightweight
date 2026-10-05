package com.zrp8858.zukelightweight.mouse;

import com.zrp8858.zukelightweight.config.Configs;
import com.zrp8858.zukelightweight.config.ScrollDirection;
import com.zrp8858.zukelightweight.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Scroll wheel over a stack: one notch moves one item between "this" inventory and the
 * "other" one (your inventory versus a chest, furnace, crafting grid...). Scrolling down
 * pushes an item out of the stack into the other inventory; scrolling up pulls a matching
 * item from the other inventory into the stack (see {@link ScrollDirection} for the
 * alternatives). Crafting-output slots move one whole crafted batch per notch.
 *
 * <p>Adapted from the wheel tweak in Mouse Tweaks by Ivan Molodetskikh (YaLTeR),
 * BSD 3-Clause; see THIRD-PARTY-NOTICES.md. Everything is done with ordinary slot clicks,
 * so it needs nothing on the server.
 */
final class ScrollTweak {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private ScrollTweak() {}

    /** @return true if the scroll was used and vanilla should not also handle it */
    static boolean onScroll(AbstractContainerScreen<?> screen, double x, double y, double scrollY) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !Configs.MouseTweaks.WHEEL_TWEAK.getBooleanValue() || scrollY == 0) {
            return false;
        }

        Slot selected = accessor(screen).zukelightweight$getHoveredSlot(x, y);
        if (selected == null || isIgnored(screen, selected, player)) {
            return false;
        }
        // From here on the scroll is ours even if nothing moves, so it doesn't suddenly
        // fall through to something else when the slot runs out of items.

        ItemStack selectedStack = selected.getItem();
        if (selectedStack.isEmpty()) {
            return true;
        }

        ScrollDirection direction = (ScrollDirection) Configs.MouseTweaks.WHEEL_DIRECTION.getOptionListValue();
        List<Slot> slots = screen.getMenu().slots;
        boolean push = scrollY < 0; // one item per notch, whatever the wheel's speed
        if (direction.isPositionAware() && otherInventoryIsAbove(selected, slots, player)) {
            push = !push;
        }
        if (direction.isInverted()) {
            push = !push;
        }

        ItemStack cursor = screen.getMenu().getCarried();
        if (isCraftingOutput(selected)) {
            scrollCraftingOutput(screen, selected, selectedStack, cursor, push, slots, player);
            return true;
        }

        // Can't cleanly work on a stack when the cursor holds more of the same item.
        if (!cursor.isEmpty() && compatible(selectedStack, cursor)) {
            return true;
        }

        if (push) {
            pushOne(screen, selected, selectedStack, cursor, slots, player);
        } else {
            pullOne(screen, selected, selectedStack, cursor, slots, player);
        }
        return true;
    }

    /** Moves one item out of {@code selected} into the other inventory. */
    private static void pushOne(AbstractContainerScreen<?> screen, Slot selected, ItemStack selectedStack,
                                ItemStack cursor, List<Slot> slots, Player player) {
        if (!cursor.isEmpty() && !selected.mayPlace(cursor)) {
            return;
        }
        List<Slot> targets = findPushSlots(screen, slots, selected, 1, false, player);
        if (targets.isEmpty()) {
            return; // nowhere to put it, so don't pick anything up
        }

        boolean hadCursorItem = !cursor.isEmpty();
        // Right-click picks up half a stack (so a furnace input keeps smelting, and a
        // bundle on the cursor swaps instead of absorbing). Left-click only when the
        // whole thing is a single item.
        int pickUp = (cursor.isEmpty() && selectedStack.getCount() <= 1) ? LEFT : RIGHT;
        click(screen, selected, pickUp);

        ItemStack picked = screen.getMenu().getCarried();
        int toMove = Math.min(1, picked.getCount());
        for (Slot target : targets) {
            int times = Math.min(target.getMaxStackSize(picked) - target.getItem().getCount(), toMove);
            toMove -= times;
            while (times-- > 0) {
                click(screen, target, RIGHT);
            }
        }
        putBack(screen, selected, hadCursorItem);
    }

    /** Moves one item from the other inventory into {@code selected}. */
    private static void pullOne(AbstractContainerScreen<?> screen, Slot selected, ItemStack selectedStack,
                                ItemStack cursor, List<Slot> slots, Player player) {
        if (selected.getMaxStackSize(selectedStack) - selectedStack.getCount() < 1) {
            return; // already full
        }
        Slot source = findPullSlot(screen, slots, selected, player);
        if (source == null) {
            return;
        }
        ItemStack sourceStack = source.getItem();

        if (isCraftingOutput(source)) {
            // One notch = one crafted batch, and only if the whole batch fits.
            if (selected.getMaxStackSize(selectedStack) - selectedStack.getCount() < sourceStack.getCount()
                    || (!cursor.isEmpty() && !selected.mayPlace(cursor))) {
                return;
            }
            click(screen, selected, LEFT);  // put the cursor stack down, pick up the selected stack
            click(screen, source, LEFT);    // the crafted batch joins it on the cursor
            click(screen, selected, LEFT);  // put everything back
            return;
        }

        boolean hadCursorItem = !cursor.isEmpty();
        if (hadCursorItem && !source.mayPlace(cursor)) {
            return;
        }
        int pickUp = (cursor.isEmpty() && sourceStack.getCount() == 1) ? LEFT : RIGHT;
        click(screen, source, pickUp);

        int pickedUp = screen.getMenu().getCarried().getCount();
        if (pickedUp <= 1) {
            click(screen, selected, LEFT);  // all of it goes in
        } else {
            click(screen, selected, RIGHT); // just one
        }
        putBack(screen, source, hadCursorItem);
    }

    /** Scrolling over a crafting result: pull a batch onto the cursor, or distribute it. */
    private static void scrollCraftingOutput(AbstractContainerScreen<?> screen, Slot output, ItemStack outputStack,
                                             ItemStack cursor, boolean push, List<Slot> slots, Player player) {
        if (!compatible(outputStack, cursor)) {
            return;
        }
        if (!cursor.isEmpty()) {
            click(screen, output, LEFT); // add a crafted batch to the cursor stack
            return;
        }
        if (!push) {
            return; // nothing can be pulled into a result slot
        }

        // The whole batch has to fit somewhere, or we don't craft at all.
        List<Slot> targets = findPushSlots(screen, slots, output, outputStack.getCount(), true, player);
        if (targets == null) {
            return;
        }
        click(screen, output, LEFT); // grab the batch
        for (int i = 0; i < targets.size(); i++) {
            Slot target = targets.get(i);
            if (i == targets.size() - 1) {
                click(screen, target, LEFT); // the rest goes in here
            } else {
                int times = target.getMaxStackSize(target.getItem()) - target.getItem().getCount();
                while (times-- > 0) {
                    click(screen, target, RIGHT);
                }
            }
        }
    }

    /**
     * Puts down any leftovers and picks the original cursor item back up, by clicking the
     * slot we borrowed from once more.
     */
    private static void putBack(AbstractContainerScreen<?> screen, Slot slot, boolean hadCursorItem) {
        boolean hasLeftovers = !screen.getMenu().getCarried().isEmpty();
        if (hadCursorItem || hasLeftovers) {
            // Swapping with a different stack: right-click so bundles swap too. Putting back
            // our own leftovers: left-click.
            click(screen, slot, hadCursorItem && hasLeftovers ? RIGHT : LEFT);
        }
    }

    /** The first non-empty stack in the other inventory that matches, searching last to first. */
    private static Slot findPullSlot(AbstractContainerScreen<?> screen, List<Slot> slots, Slot selected,
                                     Player player) {
        ItemStack selectedStack = selected.getItem();
        boolean wantPlayerInventory = selected.container != player.getInventory();
        for (int i = slots.size() - 1; i >= 0; i--) {
            Slot slot = slots.get(i);
            if (isIgnored(screen, slot, player)
                    || (slot.container == player.getInventory()) != wantPlayerInventory) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && compatible(selectedStack, stack)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Slots in the other inventory that can take {@code itemCount} items from {@code selected},
     * partly-filled matching stacks first, then empty slots that accept the item.
     *
     * @return the slots in order, or null if {@code mustDistributeAll} and they don't all fit
     */
    private static List<Slot> findPushSlots(AbstractContainerScreen<?> screen, List<Slot> slots, Slot selected,
                                            int itemCount, boolean mustDistributeAll, Player player) {
        ItemStack selectedStack = selected.getItem();
        boolean wantPlayerInventory = selected.container != player.getInventory();
        List<Slot> result = new ArrayList<>();
        List<Slot> emptySlots = new ArrayList<>();

        for (int i = 0; i < slots.size() && itemCount > 0; i++) {
            Slot slot = slots.get(i);
            if (isIgnored(screen, slot, player) || isCraftingOutput(slot)
                    || (slot.container == player.getInventory()) != wantPlayerInventory) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                if (slot.mayPlace(selectedStack)) {
                    emptySlots.add(slot);
                }
            } else if (compatible(selectedStack, stack) && stack.getCount() < slot.getMaxStackSize(stack)) {
                result.add(slot);
                itemCount -= Math.min(itemCount, slot.getMaxStackSize(stack) - stack.getCount());
            }
        }
        for (int i = 0; i < emptySlots.size() && itemCount > 0; i++) {
            Slot slot = emptySlots.get(i);
            result.add(slot);
            itemCount -= Math.min(itemCount, slot.getMaxStackSize(selectedStack));
        }
        return mustDistributeAll && itemCount > 0 ? null : result;
    }

    /** True if more of the other inventory's slots sit above {@code selected} than below it. */
    private static boolean otherInventoryIsAbove(Slot selected, List<Slot> slots, Player player) {
        boolean selectedIsPlayers = selected.container == player.getInventory();
        int above = 0;
        int below = 0;
        for (Slot slot : slots) {
            if ((slot.container == player.getInventory()) != selectedIsPlayers) {
                if (slot.y < selected.y) {
                    above++;
                } else {
                    below++;
                }
            }
        }
        return above > below;
    }

    /** Stacks can combine: either is empty, or they're the same item with the same components. */
    private static boolean compatible(ItemStack a, ItemStack b) {
        return a.isEmpty() || b.isEmpty() || ItemStack.isSameItemSameComponents(a, b);
    }

    private static boolean isCraftingOutput(Slot slot) {
        return slot instanceof ResultSlot || slot instanceof FurnaceResultSlot || slot instanceof MerchantResultSlot;
    }

    /** In the creative inventory only the player's own slots take part, not the item picker. */
    private static boolean isIgnored(AbstractContainerScreen<?> screen, Slot slot, Player player) {
        return screen instanceof CreativeModeInventoryScreen && slot.container != player.getInventory();
    }

    private static void click(AbstractContainerScreen<?> screen, Slot slot, int button) {
        accessor(screen).zukelightweight$slotClicked(slot, slot.index, button, ContainerInput.PICKUP);
    }

    private static AbstractContainerScreenAccessor accessor(AbstractContainerScreen<?> screen) {
        return (AbstractContainerScreenAccessor) screen;
    }
}
