package com.fende.obesecat.block.entity;

import com.fende.obesecat.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CreativeTabbyriumSiloBlockEntity extends TabbyriumSiloBlockEntity {
    private static final IEnergyStorage CREATIVE_ENERGY = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return maxReceive; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return maxExtract; }
        @Override public int getEnergyStored() { return CAPACITY; }
        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    };

    public CreativeTabbyriumSiloBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public static void creativeServerTick(Level level, BlockPos pos, BlockState state, CreativeTabbyriumSiloBlockEntity silo) {
        if (!silo.getItem(0).isEmpty()) {
            ItemStack stack = silo.getItem(0);
            IEnergyStorage target = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (target != null && target.canReceive()) {
                target.receiveEnergy(CAPACITY, false);
                silo.setChanged();
            }
        }
    }

    @Override
    public IEnergyStorage getEnergyStorage() {
        return CREATIVE_ENERGY;
    }
}
