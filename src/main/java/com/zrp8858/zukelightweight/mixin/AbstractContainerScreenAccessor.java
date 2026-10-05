package com.zrp8858.zukelightweight.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Lets us click slots through the screen's own {@code slotClicked}, so screens
 * that override it (the creative inventory) behave correctly, and ask the
 * screen which slot is under a point.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Invoker("getHoveredSlot")
    Slot zukelightweight$getHoveredSlot(double x, double y);

    @Invoker("slotClicked")
    void zukelightweight$slotClicked(Slot slot, int slotId, int button, ContainerInput input);
}
