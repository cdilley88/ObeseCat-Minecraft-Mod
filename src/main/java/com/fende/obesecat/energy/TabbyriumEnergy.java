package com.fende.obesecat.energy;

import com.fende.obesecat.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/** Constants and helpers for the Tabbyrium industrial energy grid. */
public final class TabbyriumEnergy {
    public static final int SHARD_ENERGY = 10_000;
    public static final int SHARD_PROCESS_TIME = 40; // 2 seconds (250 FE/t)

    public static final int CRYSTAL_ENERGY = 45_000; // Bonus efficiency (375 FE/t)
    public static final int CRYSTAL_PROCESS_TIME = 120; // 6 seconds

    public static final int REFINERY_CAPACITY = 100_000;
    public static final int REFINERY_MAX_OUTPUT = 1_000;

    public static final int SILO_CAPACITY = 1_000_000;
    public static final int SILO_MAX_TRANSFER = 10_000;

    public static int getFuelEnergy(ItemStack stack) {
        if (stack.is(ModItems.TABBYRIUM_SHARD.get())) {
            return SHARD_ENERGY;
        }
        if (stack.is(ModItems.TABBYRIUM_CRYSTAL.get())) {
            return CRYSTAL_ENERGY;
        }
        return 0;
    }

    public static int getProcessTime(ItemStack stack) {
        if (stack.is(ModItems.TABBYRIUM_SHARD.get())) {
            return SHARD_PROCESS_TIME;
        }
        if (stack.is(ModItems.TABBYRIUM_CRYSTAL.get())) {
            return CRYSTAL_PROCESS_TIME;
        }
        return 0;
    }

    private TabbyriumEnergy() {
    }
}
