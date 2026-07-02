package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class HolySwordItemTest {
    @Test
    void holySwordUsesTheHolySwordTextureAndIsNamedCorrectly() throws Exception {
        String langFile = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        String modelFile = Files.readString(Path.of("src/main/resources/assets/obesecat/models/item/holy_sword.json"));

        assertTrue(langFile.contains("\"item.obesecat.holy_sword\": \"Holy Sword\""));
        assertTrue(modelFile.contains("\"obesecat:item/holysword\""));
    }
}
