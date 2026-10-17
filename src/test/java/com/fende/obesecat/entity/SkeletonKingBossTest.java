package com.fende.obesecat.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SkeletonKingBossTest {
    @Test
    void skeletonKingAndPerfumeCatalystsConfiguredCorrectly() throws Exception {
        String skeletonKingFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/entity/SkeletonKing.java"));
        String crownFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/SkeletonCrownItem.java"));
        String modItemsFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModItems.java"));
        String lootTableFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModLootTables.java"));
        String langFile = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));

        // Altar ritual checks
        assertTrue(crownFile.contains("Blocks.BONE_BLOCK"));
        assertTrue(crownFile.contains("ModEntities.SKELETON_KING"));
        assertTrue(crownFile.contains("findAltar"));

        // Skeleton King stats and drops
        assertTrue(skeletonKingFile.contains("MAX_HEALTH, 500.0D"));
        assertTrue(skeletonKingFile.contains("SkeletonKingTeleportSlamGoal"));
        assertTrue(skeletonKingFile.contains("SkeletonKingSummonGuardsGoal"));
        assertTrue(skeletonKingFile.contains("ModItems.CHERCHE.get()"));
        assertTrue(skeletonKingFile.contains("ModItems.SALTY_RAGE.get()"));
        assertTrue(skeletonKingFile.contains("ModItems.CHANTAGE.get()"));
        assertTrue(skeletonKingFile.contains("ModItems.SETIEMSON.get()"));

        // Registration & Loot
        assertTrue(modItemsFile.contains("SKELETON_CROWN"));
        assertTrue(modItemsFile.contains("CHERCHE"));
        assertTrue(modItemsFile.contains("SALTY_RAGE"));
        assertTrue(modItemsFile.contains("CHANTAGE"));
        assertTrue(modItemsFile.contains("SETIEMSON"));
        assertTrue(lootTableFile.contains("END_CITY_SKELETON_CROWN_POOL"));

        // FFT Perfume and Crown lang
        assertTrue(langFile.contains("\"entity.obesecat.skeleton_king\": \"The Skeleton King\""));
        assertTrue(langFile.contains("\"item.obesecat.skeleton_crown\": \"Skeleton Crown\""));
        assertTrue(langFile.contains("\"item.obesecat.cherche\": \"Cherche\""));
        assertTrue(langFile.contains("\"item.obesecat.salty_rage\": \"Salty Rage\""));
        assertTrue(langFile.contains("\"item.obesecat.chantage\": \"Chantage\""));
        assertTrue(langFile.contains("\"item.obesecat.setiemson\": \"Setiemson\""));

        // Transmutation recipe files
        String blastarRecipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/blastar_punch_transmutation.json"));
        String hellcryRecipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/hellcry_punch_transmutation.json"));
        String icewolfRecipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/icewolf_bite_transmutation.json"));

        assertTrue(blastarRecipe.contains("\"item\": \"obesecat:salty_rage\""));
        assertTrue(hellcryRecipe.contains("\"item\": \"obesecat:chantage\""));
        assertTrue(icewolfRecipe.contains("\"item\": \"obesecat:setiemson\""));

        // Diablo 2 Enigma Semiconductors (Jah, Ith, Ber)
        String carverFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/entity/TheCarver.java"));
        String cowKingLoot = Files.readString(Path.of("src/main/resources/data/obesecat/loot_table/entities/cow_king.json"));
        String enigmaRecipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/enigma_ember_transmutation.json"));
        String rendererFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/client/SkeletonKingRenderer.java"));

        assertTrue(skeletonKingFile.contains("ModItems.QUANTUM_SEMICONDUCTOR.get()"));
        assertTrue(carverFile.contains("ModItems.ATOMIC_SEMICONDUCTOR.get()"));
        assertTrue(cowKingLoot.contains("obesecat:dimensional_semiconductor"));

        assertTrue(enigmaRecipe.contains("\"item\": \"obesecat:quantum_semiconductor\""));
        assertTrue(enigmaRecipe.contains("\"item\": \"obesecat:dimensional_semiconductor\""));
        assertTrue(enigmaRecipe.contains("\"item\": \"obesecat:atomic_semiconductor\""));

        // Renderer verification: authentic vanilla skeleton with 3D crown layer
        assertTrue(rendererFile.contains("textures/entity/skeleton/skeleton.png"));
        assertTrue(rendererFile.contains("SkeletonKingCrownLayer"));
        assertTrue(rendererFile.contains("scale(1.85F"));
    }
}
