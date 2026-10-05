package com.zrp8858.zukelightweight;

import com.zrp8858.zukelightweight.config.Configs;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Hotbar shuffle (carried over from block-placement-shuffler): while the
 * feature is on, every block you place randomly reselects another placeable
 * block from your hotbar, so your next placement is a surprise. Toggling it
 * (and its hotkey and status message) is handled by the malilib config.
 */
public final class HotbarShuffle {
    private static final int HOTBAR_SIZE = 9;

    /** Slot to switch to on the next client tick, or -1 for "no pending switch". */
    private static int pendingSlot = -1;

    private HotbarShuffle() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(HotbarShuffle::tick);
        UseBlockCallback.EVENT.register(HotbarShuffle::onUseBlock);
    }

    private static void tick(Minecraft mc) {
        Player player = mc.player;
        if (player == null) {
            pendingSlot = -1;
            return;
        }
        if (pendingSlot >= 0 && pendingSlot < HOTBAR_SIZE) {
            player.getInventory().setSelectedSlot(pendingSlot);
        }
        pendingSlot = -1;
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand,
                                                BlockHitResult hitResult) {
        if (!Configs.Features.HOTBAR_SHUFFLE.getBooleanValue() || !level.isClientSide() || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        // Only shuffle if the held item is actually a block (i.e. this use is a placement).
        if (Block.byItem(player.getItemInHand(hand).getItem()) == Blocks.AIR) {
            return InteractionResult.PASS;
        }

        pendingSlot = pickRandomBlockSlot(player, level.getRandom());
        return InteractionResult.PASS;
    }

    /** A random hotbar slot (0-8) holding a placeable block, or -1 if none does. */
    private static int pickRandomBlockSlot(Player player, RandomSource random) {
        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        List<Integer> candidates = new ArrayList<>(HOTBAR_SIZE);
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (Block.byItem(items.get(slot).getItem()) != Blocks.AIR) {
                candidates.add(slot);
            }
        }
        return candidates.isEmpty() ? -1 : candidates.get(random.nextInt(candidates.size()));
    }
}
