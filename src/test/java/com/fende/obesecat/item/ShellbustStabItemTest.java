package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ShellbustStabItemTest {
    @Test
    void shellbustStabUsesTheMightySwordShellAndPsxQuote() throws Exception {
        String itemFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/ShellbustStabSwordItem.java"));
        String managerFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/world/ShellbustStabManager.java"));
        String langFile = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        String modelFile = Files.readString(Path.of("src/main/resources/assets/obesecat/models/item/shellbust_stab.json"));
        String recipeFile = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/shellbust_stab_transmutation.json"));

        assertTrue(itemFile.contains("extends SkillSwordItem"));
        assertTrue(managerFile.contains("ARMOR_DROP_CHANCE = 0.50F"));
        assertTrue(managerFile.contains("EquipmentSlot.CHEST"));
        assertTrue(langFile.contains("Armor won't help the heart stay sharp..."));
        assertTrue(modelFile.contains("\"obesecat:item/mightysword\""));
        assertTrue(recipeFile.contains("\"item\": \"obesecat:mighty_sword\""));
        assertTrue(recipeFile.contains("\"item\": \"minecraft:netherite_chestplate\""));
    }
}


