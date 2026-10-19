package com.fende.obesecat.block.entity;

import com.fende.obesecat.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CreativeTabbyriumSiloBlockEntity extends BlockEntity {
    private static final IEnergyStorage CREATIVE_ENERGY = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return maxReceive; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return maxExtract; }
        @Override public int getEnergyStored() { return Integer.MAX_VALUE; }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    };

    public CreativeTabbyriumSiloBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_TABBYRIUM_SILO.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return CREATIVE_ENERGY;
    }
}
