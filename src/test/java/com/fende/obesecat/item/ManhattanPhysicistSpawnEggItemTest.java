package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ManhattanPhysicistSpawnEggItemTest {
    @Test
    void eggConfiguresPhysicistBeforeAddingItToTheLevel() throws Exception {
        String eggFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/ManhattanPhysicistSpawnEggItem.java"));
        int createCall = eggFile.indexOf("Villager villager = createPhysicist(serverLevel, spawnPos)");
        int addToLevel = eggFile.indexOf("serverLevel.addFreshEntity(villager)");

        assertTrue(createCall >= 0);
        assertTrue(addToLevel >= 0);
        assertTrue(createCall < addToLevel, "The physicist must be created and configured before client tracking begins");
        assertTrue(eggFile.contains("ManhattanPhysicistSpawner.initializeEggVillager(villager)"));
        assertTrue(!eggFile.contains("EntityType.VILLAGER.spawn("));
    }
}
