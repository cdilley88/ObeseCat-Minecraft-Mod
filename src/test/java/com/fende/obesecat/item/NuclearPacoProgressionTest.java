package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fende.obesecat.world.TacticalNuclearExplosion;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NuclearPacoProgressionTest {

    @Test
    void verifiesExplosivePacoPropertiesAndRecipe() throws Exception {
        Field rangeField = ExplosivePacoItem.class.getDeclaredField("EXPLOSION_RANGE");
        rangeField.setAccessible(true);
        assertEquals(128.0D, (double) rangeField.get(null));

        Field powerField = ExplosivePacoItem.class.getDeclaredField("EXPLOSION_POWER");
        powerField.setAccessible(true);
        assertEquals(4.0F, (float) powerField.get(null), 0.001F);

        Field delayField = ExplosivePacoItem.class.getDeclaredField("BARK_TO_EXPLOSION_DELAY_TICKS");
        delayField.setAccessible(true);
        assertEquals(15, (int) delayField.get(null));

        Field cooldownField = ExplosivePacoItem.class.getDeclaredField("COOLDOWN_TICKS");
        cooldownField.setAccessible(true);
        assertEquals(30, (int) cooldownField.get(null));

        String recipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/explosive_paco_transmutation.json"));
        assertTrue(recipe.contains("\"item\": \"obesecat:assault_paco\""));
        assertTrue(recipe.contains("\"item\": \"minecraft:tnt\""));
        assertTrue(recipe.contains("\"id\": \"obesecat:explosive_paco\""));

        String lang = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        assertTrue(lang.contains("\"item.obesecat.explosive_paco\": \"Explosive Paco\""));
        assertTrue(lang.contains("\"item.obesecat.explosive_paco.caption\""));

        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/models/item/explosive_paco.json")));
    }

    @Test
    void verifiesAtomicPacoDoubledCraterAndRecipe() throws Exception {
        // Atomic Paco crater is twice as large (radius 20, max depth 10, scorch 32)
        assertEquals(20, TacticalNuclearExplosion.CRATER_RADIUS);
        assertEquals(10, TacticalNuclearExplosion.CRATER_MAX_DEPTH);
        assertEquals(32, TacticalNuclearExplosion.PERIMETER_SCORCH_RADIUS);

        String recipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/atomic_paco_transmutation.json"));
        assertTrue(recipe.contains("\"item\": \"obesecat:explosive_paco\""));
        assertTrue(recipe.contains("\"item\": \"obesecat:trinitite\""));
        assertTrue(recipe.contains("\"id\": \"obesecat:atomic_paco\""));

        String lang = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        assertTrue(lang.contains("\"item.obesecat.atomic_paco\": \"Atomic Paco\""));
        assertTrue(lang.contains("\"item.obesecat.atomic_paco.caption\""));
    }

    @Test
    void verifiesMirvPacoPropertiesAndRecipe() throws Exception {
        Field rangeField = MirvPacoItem.class.getDeclaredField("EXPLOSION_RANGE");
        rangeField.setAccessible(true);
        assertEquals(512.0D, (double) rangeField.get(null));

        Field delayField = MirvPacoItem.class.getDeclaredField("BARK_TO_EXPLOSION_DELAY_TICKS");
        delayField.setAccessible(true);
        assertEquals(20, (int) delayField.get(null));

        Field cooldownField = MirvPacoItem.class.getDeclaredField("COOLDOWN_TICKS");
        cooldownField.setAccessible(true);
        assertEquals(120, (int) cooldownField.get(null));

        String recipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/mirv_paco_transmutation.json"));
        assertTrue(recipe.contains("\"item\": \"obesecat:atomic_paco\""));
        assertTrue(recipe.contains("\"item\": \"minecraft:firework_rocket\""));
        assertTrue(recipe.contains("\"id\": \"obesecat:mirv_paco\""));

        String lang = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        assertTrue(lang.contains("\"item.obesecat.mirv_paco\": \"MIRV Paco\""));
        assertTrue(lang.contains("\"item.obesecat.mirv_paco.caption\""));

        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/models/item/mirv_paco.json")));
    }

    @Test
    void verifiesGuideBooksContainNuclearPacoProgression() throws Exception {
        String dataPaco = Files.readString(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/companion_entities/paco_line.json"));
        assertTrue(dataPaco.contains("Explosive Paco"));
        assertTrue(dataPaco.contains("Atomic Paco"));
        assertTrue(dataPaco.contains("MIRV Paco"));

        String assetPaco = Files.readString(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/companion_entities/paco_line.json"));
        assertTrue(assetPaco.contains("Explosive Paco"));
        assertTrue(assetPaco.contains("Atomic Paco"));
        assertTrue(assetPaco.contains("MIRV Paco"));

        String guideBookContents = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/GuideBookContents.java"));
        assertTrue(guideBookContents.contains("Explosive:§0 Assault + TNT"));
        assertTrue(guideBookContents.contains("Atomic:§0 Explosive + Trinitite"));
        assertTrue(guideBookContents.contains("MIRV:§0 Atomic + Firework Rocket"));
    }

    @Test
    void verifiesHitscanAimingToleranceDeclared() throws Exception {
        java.lang.reflect.Method baseMethod = PacoItem.class.getDeclaredMethod("getHitboxWiggleRoom");
        assertTrue(baseMethod != null);

        java.lang.reflect.Method concussiveMethod = ConcussivePacoItem.class.getDeclaredMethod("getHitboxWiggleRoom");
        assertTrue(concussiveMethod != null);

        java.lang.reflect.Method attackMethod = AttackPacoItem.class.getDeclaredMethod("getHitboxWiggleRoom");
        assertTrue(attackMethod != null);

        java.lang.reflect.Method sniperMethod = SniperPacoItem.class.getDeclaredMethod("getHitboxWiggleRoom");
        assertTrue(sniperMethod != null);
    }
}
