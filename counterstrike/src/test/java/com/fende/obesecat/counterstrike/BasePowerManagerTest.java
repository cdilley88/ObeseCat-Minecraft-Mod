package com.fende.obesecat.counterstrike;

import com.fende.obesecat.counterstrike.power.BasePowerGrid;
import com.fende.obesecat.counterstrike.power.BasePowerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BasePowerManagerTest {

    @Test
    @DisplayName("Verifies BasePowerGrid generation, consumption, and low power calculation")
    void testBasePowerGridCalculations() {
        UUID player1 = UUID.randomUUID();
        BasePowerGrid grid = new BasePowerGrid(player1);

        assertEquals(0, grid.getGenerated());
        assertEquals(0, grid.getConsumed());
        assertFalse(grid.isLowPower(), "0 consumption should not trigger low power");
        assertEquals(1.0f, grid.getEfficiency());

        // Add 1 turbine (+200 power)
        grid.addGenerated(200);
        assertEquals(200, grid.getGenerated());
        assertFalse(grid.isLowPower());
        assertEquals(1.0f, grid.getEfficiency());

        // Add 1 ConYard drain (25 power)
        grid.addConsumed(25);
        assertEquals(25, grid.getConsumed());
        assertFalse(grid.isLowPower(), "200 generated >= 25 consumed is optimal power");
        assertEquals(1.0f, grid.getEfficiency());

        // Add heavy consumers (250 total consumed > 200 generated)
        grid.addConsumed(225);
        assertEquals(250, grid.getConsumed());
        assertTrue(grid.isLowPower(), "250 consumed > 200 generated MUST trigger Low Power mode!");
        assertEquals(200f / 250f, grid.getEfficiency(), 0.001f);
    }

    @Test
    @DisplayName("Verifies BasePowerManager generator/consumer registration and NBT serialization")
    void testManagerRegistrationAndNbt() {
        BasePowerManager manager = new BasePowerManager();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        BlockPos turbinePos1 = new BlockPos(10, 64, 10);
        BlockPos turbinePos2 = new BlockPos(20, 64, 20);
        BlockPos conYardPos = new BlockPos(30, 64, 30);

        // Register player 1 assets
        manager.registerGenerator(turbinePos1, player1, 200);
        manager.registerConsumer(conYardPos, player1, 25);

        assertEquals(200, manager.getGrid(player1).getGenerated());
        assertEquals(25, manager.getGrid(player1).getConsumed());
        assertFalse(manager.getGrid(player1).isLowPower());

        // Player 2 has no power yet
        assertEquals(0, manager.getGrid(player2).getGenerated());

        // Add player 1 second turbine
        manager.registerGenerator(turbinePos2, player1, 200);
        assertEquals(400, manager.getGrid(player1).getGenerated());

        // Serialize to NBT
        CompoundTag tag = manager.save(new CompoundTag(), HolderLookup.Provider.create(java.util.stream.Stream.empty()));

        // Load into fresh manager instance
        BasePowerManager loaded = BasePowerManager.load(tag, HolderLookup.Provider.create(java.util.stream.Stream.empty()));

        assertEquals(400, loaded.getGrid(player1).getGenerated());
        assertEquals(25, loaded.getGrid(player1).getConsumed());
        assertFalse(loaded.getGrid(player1).isLowPower());

        // Unregister one turbine from loaded
        loaded.unregisterGenerator(turbinePos1);
        assertEquals(200, loaded.getGrid(player1).getGenerated());
    }
}
