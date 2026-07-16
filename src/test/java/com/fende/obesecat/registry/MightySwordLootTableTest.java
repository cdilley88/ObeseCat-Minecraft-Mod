package com.fende.obesecat.registry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MightySwordLootTableTest {
    @Test
    void mightySwordIsInjectedIntoAncientCityLoot() throws Exception {
        String lootTableFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModLootTables.java"));

        assertTrue(lootTableFile.contains("ANCIENT_CITY_MIGHTY_SWORD_POOL"));
        assertTrue(lootTableFile.contains("ModItems.MIGHTY_SWORD.get()"));
    }
}
