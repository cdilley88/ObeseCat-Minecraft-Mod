package com.fende.obesecat.inventory;

import com.fende.obesecat.item.TransmutationCubeItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

public final class TransmutationCubeInventory extends SimpleContainer {
    public static final int SLOT_COUNT = 12;

    private final ItemStack cubeStack;

    public TransmutationCubeInventory(ItemStack cubeStack) {
        super(SLOT_COUNT);
        this.cubeStack = cubeStack;
        cubeStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(getItems());
        addListener(container -> save());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof TransmutationCubeItem);
    }

    public void replaceContents(List<ItemStack> contents) {
        if (contents.size() != SLOT_COUNT) {
            throw new IllegalArgumentException("Cube replacement must contain exactly 12 slots");
        }

        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            getItems().set(slot, contents.get(slot).copy());
        }
        setChanged();
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack removed = super.removeItemNoUpdate(index);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    private void save() {
        cubeStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }
}
