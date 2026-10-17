package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DarkSwordSkillTest {
    @Test
    void darkSwordUsesTrackingCrescentAndFutureReadyDrainReward() throws Exception {
        String item = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/DarkSwordItem.java"));
        String manager = Files.readString(Path.of("src/main/java/com/fende/obesecat/world/DarkSwordManager.java"));
        String lang = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        String sounds = Files.readString(Path.of("src/main/resources/assets/obesecat/sounds.json"));

        assertTrue(item.contains("DarkSwordManager.schedule"));
        assertTrue(item.contains("findEntityTarget"));
        assertTrue(item.contains("findBlockTarget"));
        assertTrue(manager.contains("ANIMATION_TICKS = 80"));
        assertTrue(manager.contains("STAB_TICK = 40"));
        assertTrue(manager.contains("TWIST_DEGREES = 160.0F"));
        assertTrue(manager.contains("anchor.add(0.0D, -3.0D, 0.0D)"));
        assertTrue(manager.contains("applyDrainReward"));
        assertTrue(manager.contains("Future MP/resource restoration belongs here"));
        assertTrue(lang.contains("Dead or alive... slash magic power! Dark Sword!"));
        assertTrue(lang.contains("\"item.obesecat.dark_sword.effect\": \"Steals NULL\""));
        assertTrue(lang.contains("\"item.obesecat.night_sword.effect\": \"Steals Life\""));
        assertTrue(sounds.contains("obesecat:item/darksworddrone"));
        assertTrue(sounds.contains("obesecat:item/darkswordstab"));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/textures/item/dark_sword_blade_gfx.png")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/sounds/item/darksworddrone.ogg")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/sounds/item/darkswordstab.ogg")));

        // Dark Sword Base & The Carver Questline Assertions
        String registry = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModItems.java"));
        String entities = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModEntities.java"));
        String loot = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModLootTables.java"));
        String darkRecipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/dark_sword_transmutation.json"));

        assertTrue(registry.contains("DeferredItem<DarkSwordBaseItem> DARK_SWORD_BASE"));
        assertTrue(registry.contains("DeferredItem<CursedCleaverItem> CURSED_CLEAVER"));
        assertTrue(registry.contains("DeferredItem<CaptionedItem> FLAYED_DEMON_HEART"));
        assertTrue(registry.contains("DeferredItem<CaptionedItem> BLOODIED_IRON_HOOK"));
        assertTrue(entities.contains("THE_CARVER"));
        assertTrue(loot.contains("ANCIENT_CITY_DARK_SWORD_POOL"));
        assertTrue(loot.contains("BASTION_CURSED_CLEAVER_POOL"));
        assertTrue(darkRecipe.contains("\"item\": \"obesecat:dark_sword_base\""));
        assertTrue(darkRecipe.contains("\"item\": \"obesecat:bloodied_iron_hook\""));
        assertTrue(darkRecipe.contains("\"id\": \"obesecat:dark_sword\""));
    }
}
