package com.fende.obesecat.energy;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Stack-persistent, externally chargeable FE storage for castable items. */
public final class CastItemEnergy {
    public static final int CASTS_PER_FULL_CHARGE = 10;
    private static final String ENERGY_KEY = "CastItemEnergy";

    private CastItemEnergy() {
    }

    public static int capacityFor(int castCost) {
        return Math.multiplyExact(castCost, CASTS_PER_FULL_CHARGE);
    }

    public static int getEnergy(ItemStack stack, int capacity) {
        return Math.max(0, Math.min(capacity, getTag(stack).getInt(ENERGY_KEY)));
    }

    public static boolean hasEnergy(ItemStack stack, int castCost) {
        return castCost <= 0 || getEnergy(stack, capacityFor(castCost)) >= castCost;
    }

    public static boolean consume(ItemStack stack, int castCost) {
        if (!hasEnergy(stack, castCost)) {
            return false;
        }
        setEnergy(stack, capacityFor(castCost), getEnergy(stack, capacityFor(castCost)) - castCost);
        return true;
    }

    public static IEnergyStorage createStorage(ItemStack stack, int capacity) {
        return new StackEnergyStorage(stack, capacity);
    }

    private static void setEnergy(ItemStack stack, int capacity, int energy) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putInt(ENERGY_KEY, Math.max(0, Math.min(capacity, energy))));
    }

    private static CompoundTag getTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private record StackEnergyStorage(ItemStack stack, int capacity) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int stored = getEnergy(stack, capacity);
            int received = Math.min(Math.max(maxReceive, 0), capacity - stored);
            if (!simulate && received > 0) {
                setEnergy(stack, capacity, stored + received);
            }
            return received;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return getEnergy(stack, capacity); }
        @Override public int getMaxEnergyStored() { return capacity; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}
