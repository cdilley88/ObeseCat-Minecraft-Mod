package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MightySwordItemTest {
    @Test
    void mightySwordMirrorsTheHolySwordBaseAndUsesItsTexture() throws Exception {
        String itemFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/MightySwordItem.java"));
        String registryFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModItems.java"));
        String langFile = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        String modelFile = Files.readString(Path.of("src/main/resources/assets/obesecat/models/item/mighty_sword.json"));

        assertTrue(itemFile.contains("extends SkillSwordItem"));
        assertTrue(registryFile.contains("DeferredItem<MightySwordItem> MIGHTY_SWORD"));
        assertTrue(registryFile.contains("skillSwordProperties(Rarity.UNCOMMON)"));
        assertTrue(langFile.contains("\"item.obesecat.mighty_sword\": \"Mighty Sword\""));
        assertTrue(modelFile.contains("\"obesecat:item/mightysword\""));
    }
}
