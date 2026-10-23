package com.fende.obesecat.block.entity;

import com.fende.obesecat.block.TabbyriumSiloBlock;
import com.fende.obesecat.energy.TabbyriumEnergy;
import com.fende.obesecat.inventory.TabbyriumSiloMenu;
import com.fende.obesecat.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
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

public class TabbyriumSiloBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int CAPACITY = TabbyriumEnergy.SILO_CAPACITY;
    public static final int MAX_TRANSFER = TabbyriumEnergy.SILO_MAX_TRANSFER;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final SiloEnergyStorage energy = new SiloEnergyStorage();

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.stored & 0xFFFF;
                case 1 -> (energy.stored >> 16) & 0xFFFF;
                case 2 -> CAPACITY & 0xFFFF;
                case 3 -> (CAPACITY >> 16) & 0xFFFF;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            int currentStored = energy.stored;
            if (index == 0) {
                energy.stored = (currentStored & ~0xFFFF) | (value & 0xFFFF);
            } else if (index == 1) {
                energy.stored = ((value & 0xFFFF) << 16) | (currentStored & 0xFFFF);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public TabbyriumSiloBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TABBYRIUM_SILO.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TabbyriumSiloBlockEntity silo) {
        // Charging slot logic: transfer power from silo to slotted item
        if (silo.energy.stored > 0 && !silo.items.getFirst().isEmpty()) {
            ItemStack stack = silo.items.getFirst();
            IEnergyStorage target = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (target != null && target.canReceive()) {
                int offered = Math.min(MAX_TRANSFER, silo.energy.stored);
                int accepted = target.receiveEnergy(offered, true);
                if (accepted > 0) {
                    silo.energy.stored -= accepted;
                    target.receiveEnergy(accepted, false);
                    silo.setChanged();
                }
            }
        }

        // Update blockstate fill level (0 = empty, 1 = 25%, 2 = 50%, 3 = 75%, 4 = 100%)
        int currentStored = silo.energy.stored;
        int levelVal;
        if (currentStored <= 0) {
            levelVal = 0;
        } else if (currentStored < 250_000) {
            levelVal = 1;
        } else if (currentStored < 500_000) {
            levelVal = 2;
        } else if (currentStored < 950_000) {
            levelVal = 3;
        } else {
            levelVal = 4;
        }

        if (state.hasProperty(TabbyriumSiloBlock.LEVEL) && state.getValue(TabbyriumSiloBlock.LEVEL) != levelVal) {
            level.setBlock(pos, state.setValue(TabbyriumSiloBlock.LEVEL, levelVal), 3);
        }
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, new SimpleContainer(items.toArray(ItemStack[]::new)));
        clearContent();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Energy", energy.stored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        energy.stored = Math.max(0, Math.min(CAPACITY, tag.getInt("Energy")));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.obesecat.tabbyrium_silo");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TabbyriumSiloMenu(id, inventory, this, data);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.getFirst().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(1);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    private final class SiloEnergyStorage implements IEnergyStorage {
        private int stored;

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(Math.max(maxReceive, 0), Math.min(CAPACITY - stored, MAX_TRANSFER));
            if (!simulate && received > 0) {
                stored += received;
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(Math.max(maxExtract, 0), Math.min(stored, MAX_TRANSFER));
            if (!simulate && extracted > 0) {
                stored -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override public int getEnergyStored() { return stored; }
        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    }
}
