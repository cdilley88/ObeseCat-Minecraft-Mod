package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NightSwordItemTest {
    @Test
    void darkSwordBranchUsesSuppliedAssetsAndNightSwordTiming() throws Exception {
        String registry = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModItems.java"));
        String manager = Files.readString(Path.of("src/main/java/com/fende/obesecat/world/NightSwordManager.java"));
        String item = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/NightSwordItem.java"));
        String lang = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        String sounds = Files.readString(Path.of("src/main/resources/assets/obesecat/sounds.json"));
        String recipe = Files.readString(Path.of(
                "src/main/resources/data/obesecat/recipe/night_sword_transmutation.json"));

        assertTrue(registry.contains("DeferredItem<DarkSwordItem> DARK_SWORD"));
        assertTrue(registry.contains("DeferredItem<NightSwordItem> NIGHT_SWORD"));
        assertTrue(item.contains("item.obesecat.skill_class.dark_sword"));
        assertTrue(manager.contains("ANIMATION_TICKS = 80"));
        assertTrue(manager.contains("STAB_TICK = 40"));
        assertTrue(manager.contains("DAMAGE = 10.0F"));
        assertTrue(manager.contains("DAMAGE_RADIUS = 3.5D"));
        assertTrue(manager.contains("HEAL_AMOUNT = 4.0F"));
        assertTrue(manager.contains("cast.damagedEnemy = damageArea(level, cast.anchor)"));
        assertTrue(manager.contains("cast.damagedEnemy && caster != null"));
        assertTrue(lang.contains("Master of all swords, cut energy! Night Sword!"));
        assertTrue(sounds.contains("obesecat:item/nightsworddrone"));
        assertTrue(sounds.contains("obesecat:item/nightswordstab"));
        assertTrue(recipe.contains("\"item\": \"obesecat:dark_sword\""));
        assertTrue(recipe.contains("\"item\": \"minecraft:dirt\""));
        assertTrue(recipe.contains("\"id\": \"obesecat:night_sword\""));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/textures/item/dark_sword.png")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/textures/item/night_sword.png")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/textures/item/night_sword_skull_gfx.png")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/sounds/item/nightsworddrone.ogg")));
    }
}
