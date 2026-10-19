package com.fende.obesecat.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TabbyriumEnergyTest {
    @Test
    void verifiesTabbyriumFuelValues() {
        assertEquals(10_000, TabbyriumEnergy.SHARD_ENERGY);
        assertEquals(40, TabbyriumEnergy.SHARD_PROCESS_TIME);
        assertEquals(45_000, TabbyriumEnergy.CRYSTAL_ENERGY);
        assertEquals(120, TabbyriumEnergy.CRYSTAL_PROCESS_TIME);
    }

    @Test
    void verifiesGridCapacities() {
        assertEquals(100_000, TabbyriumEnergy.REFINERY_CAPACITY);
        assertEquals(1_000, TabbyriumEnergy.REFINERY_MAX_OUTPUT);
        assertEquals(1_000_000, TabbyriumEnergy.SILO_CAPACITY);
        assertEquals(10_000, TabbyriumEnergy.SILO_MAX_TRANSFER);
    }
}
