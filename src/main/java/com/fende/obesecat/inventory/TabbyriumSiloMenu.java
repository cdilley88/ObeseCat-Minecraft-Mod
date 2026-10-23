package com.fende.obesecat.inventory;

import com.fende.obesecat.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

public class TabbyriumSiloMenu extends AbstractContainerMenu {
    private final Container silo;
    private final ContainerData data;

    public static TabbyriumSiloMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        buffer.readBlockPos();
        return new TabbyriumSiloMenu(id, inventory, new SimpleContainer(1), new SimpleContainerData(4));
    }

    public TabbyriumSiloMenu(int id, Inventory inventory, Container silo, ContainerData data) {
        super(ModMenus.TABBYRIUM_SILO.get(), id);
        this.silo = silo;
        this.data = data;
        checkContainerSize(silo, 1);
        checkContainerDataCount(data, 4);
        silo.startOpen(inventory.player);

        // Charging Slot at x=44, y=44
        addSlot(new Slot(silo, 0, 44, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 94 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 152));
        }

        addDataSlots(data);
    }

    public int getEnergy() {
        return (data.get(1) << 16) | (data.get(0) & 0xFFFF);
    }

    public int getCapacity() {
        return (data.get(3) << 16) | (data.get(2) & 0xFFFF);
    }

    public int getEnergyBarHeight(int height) {
        int cap = getCapacity();
        return cap <= 0 ? 0 : (int) ((long) getEnergy() * height / cap);
    }

    public boolean isFull() {
        return getEnergy() >= getCapacity() && getCapacity() > 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return silo.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        silo.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index == 0) {
            if (!moveItemStackTo(source, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (source.getCapability(Capabilities.EnergyStorage.ITEM) != null) {
            if (!moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
}
