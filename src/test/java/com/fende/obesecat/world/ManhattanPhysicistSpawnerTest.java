package com.fende.obesecat.world;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ManhattanPhysicistSpawnerTest {
    @Test
    void reloadNormalizationPreservesUsedOffersAndLocksProfession() throws Exception {
        String spawnerFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/world/ManhattanPhysicistSpawner.java"));
        String normalization = spawnerFile.substring(
                spawnerFile.indexOf("private static void normalizeExistingVillager"),
                spawnerFile.indexOf("private static void setProfession")
        );

        assertTrue(normalization.contains("villager.getOffers().isEmpty()"));
        assertTrue(!normalization.contains("setVillagerXp(0)"));
        assertTrue(normalization.contains("PHYSICIST_ENTITY_TAG"));
        assertTrue(normalization.contains("setVillagerXp(Math.max(1, villager.getVillagerXp()))"));
        assertTrue(spawnerFile.contains("setProfession(ModVillagers.MANHATTAN_PHYSICIST.get())"));
    }
}
