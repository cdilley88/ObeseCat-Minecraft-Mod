package com.fende.obesecat.energy;

import com.fende.obesecat.item.CatFoodFuel;
import net.minecraft.world.item.ItemStack;

public final class CatFoodEnergy {
    public static final int FE_PER_POINT = 100;
    public static final int PLUTONIUM_POINTS = 25;
    public static final int LITHIUM_DEUTERIDE_POINTS = 100;
    public static final int FAT_MAN_DETONATION_POINTS = 300;
    public static final int FAT_MAN_CAPACITY_POINTS = 500;
    public static final int CAN_OPENER_CAPACITY_POINTS = 1_000;

    public static int toFe(int catFoodPoints) {
        return catFoodPoints * FE_PER_POINT;
    }

    public static int toPoints(int forgeEnergy) {
        return forgeEnergy / FE_PER_POINT;
    }

    public static int getFuelPoints(ItemStack stack) {
        return stack.getItem() instanceof CatFoodFuel fuel ? fuel.getCatFoodPoints() : 0;
    }

    private CatFoodEnergy() {
    }
}
