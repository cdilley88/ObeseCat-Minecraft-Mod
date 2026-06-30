package com.fende.obesecat.inventory;

import com.fende.obesecat.item.TransmutationCubeItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class TransmutationCubeSlot extends Slot {
    public TransmutationCubeSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !(stack.getItem() instanceof TransmutationCubeItem)
                && container.canPlaceItem(getContainerSlot(), stack);
    }
}
