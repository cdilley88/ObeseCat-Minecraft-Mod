package com.fende.obesecat.block.entity;

import com.fende.obesecat.inventory.CatChargerMenu;
import com.fende.obesecat.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CatChargerBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int CAPACITY = 500_000;
    public static final int MAX_INPUT = 10_000;
    public static final int CHARGE_RATE = 200;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final ChargerEnergyStorage energy = new ChargerEnergyStorage();

    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? energy.stored : CAPACITY; }
        @Override public void set(int index, int value) { if (index == 0) energy.stored = Math.max(0, Math.min(CAPACITY, value)); }
        @Override public int getCount() { return 2; }
    };

    public CatChargerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CAT_CHARGER.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CatChargerBlockEntity charger) {
        if (charger.energy.stored <= 0) return;
        ItemStack stack = charger.items.getFirst();
        IEnergyStorage target = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (target == null || !target.canReceive()) return;
        int offered = Math.min(CHARGE_RATE, charger.energy.stored);
        int accepted = target.receiveEnergy(offered, true);
        if (accepted <= 0) return;
        charger.energy.stored -= accepted;
        target.receiveEnergy(accepted, false);
        charger.setChanged();
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public void dropContents(Level level, BlockPos pos) {
        net.minecraft.world.Containers.dropContents(level, pos, new SimpleContainer(items.toArray(ItemStack[]::new)));
        clearContent();
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Energy", energy.stored);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        energy.stored = Math.max(0, Math.min(CAPACITY, tag.getInt("Energy")));
    }
    @Override public Component getDisplayName() { return Component.translatable("block.obesecat.cat_charger"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new CatChargerMenu(id, inventory, this, data); }
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.getFirst().isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(1); setChanged(); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == 0 && stack.getCapability(Capabilities.EnergyStorage.ITEM) != null; }
    @Override public void clearContent() { items.clear(); setChanged(); }

    private final class ChargerEnergyStorage implements IEnergyStorage {
        private int stored;
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(Math.max(maxReceive, 0), Math.min(CAPACITY - stored, MAX_INPUT));
            if (!simulate && received > 0) { stored += received; setChanged(); }
            return received;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return stored; }
        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}
