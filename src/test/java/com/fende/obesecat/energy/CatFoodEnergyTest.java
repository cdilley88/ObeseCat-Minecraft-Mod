package com.fende.obesecat.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CatFoodEnergyTest {
    @Test
    void convertsCatFoodPointsToForgeEnergy() {
        assertEquals(100, CatFoodEnergy.toFe(1));
        assertEquals(2_500, CatFoodEnergy.toFe(CatFoodEnergy.PLUTONIUM_POINTS));
        assertEquals(10_000, CatFoodEnergy.toFe(CatFoodEnergy.LITHIUM_DEUTERIDE_POINTS));
        assertEquals(30_000, CatFoodEnergy.toFe(CatFoodEnergy.FAT_MAN_DETONATION_POINTS));
    }

    @Test
    void convertsWholeForgeEnergyUnitsBackToCatFoodPoints() {
        assertEquals(25, CatFoodEnergy.toPoints(2_500));
        assertEquals(100, CatFoodEnergy.toPoints(10_000));
        assertEquals(300, CatFoodEnergy.toPoints(30_000));
    }
}
