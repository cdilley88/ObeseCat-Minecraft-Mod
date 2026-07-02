package com.fende.obesecat.registry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class HolySwordLootTableTest {
    @Test
    void holySwordIsInjectedIntoAncientCityLoot() throws Exception {
        String lootTableFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModLootTables.java"));

        assertTrue(lootTableFile.contains("BuiltInLootTables.ANCIENT_CITY"));
        assertTrue(lootTableFile.contains("ModItems.HOLY_SWORD.get()"));
    }
}
