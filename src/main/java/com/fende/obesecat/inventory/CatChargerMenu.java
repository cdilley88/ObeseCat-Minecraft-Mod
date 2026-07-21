package com.fende.obesecat.inventory;

import com.fende.obesecat.block.entity.CatChargerBlockEntity;
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

public class CatChargerMenu extends AbstractContainerMenu {
    private final Container charger;
    private final ContainerData data;

    public static CatChargerMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        buffer.readBlockPos();
        return new CatChargerMenu(id, inventory, new SimpleContainer(1), new SimpleContainerData(2));
    }
    public CatChargerMenu(int id, Inventory inventory, Container charger, ContainerData data) {
        super(ModMenus.CAT_CHARGER.get(), id);
        this.charger = charger;
        this.data = data;
        checkContainerSize(charger, 1);
        checkContainerDataCount(data, 2);
        charger.startOpen(inventory.player);
        addSlot(new Slot(charger, 0, 44, 44) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 94 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 152));
        addDataSlots(data);
    }
    public int getEnergy() { return data.get(0); }
    public int getCapacity() { return data.get(1); }
    public int getEnergyBarHeight(int height) { return getCapacity() <= 0 ? 0 : getEnergy() * height / getCapacity(); }
    @Override public boolean stillValid(Player player) { return charger.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); charger.stopOpen(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
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
