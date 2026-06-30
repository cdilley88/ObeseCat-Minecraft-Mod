package com.fende.obesecat.recipe;

import com.fende.obesecat.inventory.TransmutationCubeInventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

public final class TransmutationInput implements RecipeInput {
    private final List<ItemStack> items;

    private TransmutationInput(List<ItemStack> items) {
        if (items.size() != TransmutationCubeInventory.SLOT_COUNT) {
            throw new IllegalArgumentException("A transmutation input must contain exactly 12 slots");
        }
        this.items = items.stream().map(ItemStack::copy).toList();
    }

    public static TransmutationInput copyOf(Container container) {
        if (container.getContainerSize() != TransmutationCubeInventory.SLOT_COUNT) {
            throw new IllegalArgumentException("A transmutation input must contain exactly 12 slots");
        }

        List<ItemStack> items = new ArrayList<>(TransmutationCubeInventory.SLOT_COUNT);
        for (int slot = 0; slot < TransmutationCubeInventory.SLOT_COUNT; slot++) {
            items.add(container.getItem(slot));
        }
        return new TransmutationInput(items);
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    public int lowestOccupiedSlot() {
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }
}
