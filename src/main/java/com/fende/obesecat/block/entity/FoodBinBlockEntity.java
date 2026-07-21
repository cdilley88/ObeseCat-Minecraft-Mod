package com.fende.obesecat.block.entity;

import com.fende.obesecat.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class FoodBinBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1_000_000;
    public static final int MAX_TRANSFER = 10_000;
    private final FoodBinEnergyStorage energy = new FoodBinEnergyStorage();

    public FoodBinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOOD_BIN.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.stored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.stored = Math.max(0, Math.min(CAPACITY, tag.getInt("Energy")));
    }

    private final class FoodBinEnergyStorage implements IEnergyStorage {
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
