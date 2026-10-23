package com.fende.obesecat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FinalPolishPassTest {

    @Test
    void verifiesToiletBlock4WayPlacement() throws Exception {
        String toiletFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/ToiletBlock.java"));
        assertTrue(toiletFile.contains("FACING = BlockStateProperties.HORIZONTAL_FACING"));
        assertTrue(toiletFile.contains("getStateForPlacement"));

        String blockstate = Files.readString(Path.of("src/main/resources/assets/obesecat/blockstates/toilet.json"));
        assertTrue(blockstate.contains("facing=north"));
        assertTrue(blockstate.contains("facing=east"));
        assertTrue(blockstate.contains("facing=south"));
        assertTrue(blockstate.contains("facing=west"));
    }

    @Test
    void verifiesSiloChargerIncorporationAndRecipe() throws Exception {
        // Tabbyrium Silo recipe uses trinitite block
        String recipe = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/tabbyrium_silo.json"));
        assertTrue(recipe.contains("\"obesecat:trinitite\""));
        assertFalse(recipe.contains("redstone_block"));

        // Cat charger block is removed
        assertFalse(Files.exists(Path.of("src/main/java/com/fende/obesecat/block/CatChargerBlock.java")));
        assertFalse(Files.exists(Path.of("src/main/resources/data/obesecat/recipe/cat_charger.json")));

        // Tabbyrium silo block and entity incorporate charging and fill levels
        String siloBlock = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumSiloBlock.java"));
        assertTrue(siloBlock.contains("IntegerProperty LEVEL = IntegerProperty.create(\"level\", 0, 4)"));
        assertTrue(siloBlock.contains("openMenu"));

        String siloEntity = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/entity/TabbyriumSiloBlockEntity.java"));
        assertTrue(siloEntity.contains("implements Container, MenuProvider"));
        assertTrue(siloEntity.contains("Capabilities.EnergyStorage.ITEM"));

        String siloItem = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/TabbyriumSiloItem.java"));
        assertTrue(siloItem.contains("isBarVisible"));
        assertTrue(siloItem.contains("getBarWidth"));

        // Screen contains EVA warning
        String siloScreen = Files.readString(Path.of("src/main/java/com/fende/obesecat/client/TabbyriumSiloScreen.java"));
        assertTrue(siloScreen.contains("SILOS NEEDED!"));
    }

    @Test
    void verifiesEmergencyStarterHomeTradeForAllProfessions() throws Exception {
        String tradesFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModVillagerTrades.java"));
        int starterIndex = tradesFile.indexOf("ModItems.EMERGENCY_STARTER_HOME.get()");
        int farmerCheckIndex = tradesFile.indexOf("if (event.getType() != VillagerProfession.FARMER)");
        assertTrue(starterIndex > 0 && starterIndex < farmerCheckIndex,
                "Emergency Starter Home trade must be registered before the Farmer-only profession check");
    }

    @Test
    void verifiesSemiconductorDoritoRecipes() throws Exception {
        String nvg = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/night_vision_dorito_transmutation.json"));
        assertTrue(nvg.contains("\"obesecat:quantum_semiconductor\""));
        assertTrue(nvg.contains("\"minecraft:tinted_glass\""));
        assertTrue(nvg.contains("\"minecraft:amethyst_shard\""));

        String telemetry = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/telemetry_dorito_transmutation.json"));
        assertTrue(telemetry.contains("\"obesecat:dimensional_semiconductor\""));
        assertTrue(telemetry.contains("\"minecraft:tinted_glass\""));
        assertTrue(telemetry.contains("\"minecraft:amethyst_shard\""));

        String scanner = Files.readString(Path.of("src/main/resources/data/obesecat/recipe/scanner_dorito_transmutation.json"));
        assertTrue(scanner.contains("\"obesecat:atomic_semiconductor\""));
        assertTrue(scanner.contains("\"minecraft:tinted_glass\""));
        assertTrue(scanner.contains("\"minecraft:amethyst_shard\""));
    }

    @Test
    void verifiesAdvancementsAndPatchouliGuides() throws Exception {
        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/advancement/silos_needed.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/advancement/night_vision_dorito.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/advancement/telemetry_dorito.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/advancement/scanner_dorito.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/advancement/dungeon_toilet.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/advancement/deploy_starter_home.json")));

        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/companion_entities/starter_homes.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/companion_entities/starter_homes.json")));
    }

    @Test
    void verifiesDungeonChocolatePuddingLootInjection() throws Exception {
        String lootFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModLootTables.java"));
        assertTrue(lootFile.contains("DUNGEON_CHOCOLATE_PUDDING_POOL"));
        assertTrue(lootFile.contains("ModItems.CHOCOLATE_PUDDING.get()"));
        assertTrue(lootFile.contains("BuiltInLootTables.SIMPLE_DUNGEON"));
    }

    @Test
    void verifiesSurfaceBunkersAndCorrectRecipesInGuide() throws Exception {
        String dataBunkers = Files.readString(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/manhattan_project/bunkers.json"));
        assertTrue(dataBunkers.contains("Overworld surface"));
        assertFalse(dataBunkers.contains("Deep underground"));

        String assetBunkers = Files.readString(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/manhattan_project/bunkers.json"));
        assertTrue(assetBunkers.contains("Overworld surface"));
        assertFalse(assetBunkers.contains("Deep underground"));

        String dataTabbyrium = Files.readString(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/manhattan_project/tabbyrium.json"));
        assertTrue(dataTabbyrium.contains("4 Trinitite"));
        assertTrue(dataTabbyrium.contains("2 Overdrive Powder"));
        assertTrue(dataTabbyrium.contains("Redstone Dust"));

        String assetTabbyrium = Files.readString(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/manhattan_project/tabbyrium.json"));
        assertTrue(assetTabbyrium.contains("4 Trinitite"));
        assertTrue(assetTabbyrium.contains("2 Overdrive Powder"));
        assertTrue(assetTabbyrium.contains("Redstone Dust"));

        String dataHomes = Files.readString(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/companion_entities/starter_homes.json"));
        assertTrue(dataHomes.contains("Block of Iron"));
        assertTrue(dataHomes.contains("Block of Gold"));
        assertTrue(dataHomes.contains("Block of Diamond"));
        assertTrue(dataHomes.contains("Block of Emerald"));

        String assetHomes = Files.readString(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/companion_entities/starter_homes.json"));
        assertTrue(assetHomes.contains("Block of Iron"));
        assertTrue(assetHomes.contains("Block of Gold"));
        assertTrue(assetHomes.contains("Block of Diamond"));
        assertTrue(assetHomes.contains("Block of Emerald"));
    }

    @Test
    void verifiesMythicBladeArtsDedicatedSectionsAndSummoning() throws Exception {
        Path dataMythic = Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/mythic_blades");
        Path assetMythic = Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/mythic_blades");

        assertTrue(Files.exists(dataMythic.resolve("holy_sword.json")));
        assertTrue(Files.exists(assetMythic.resolve("holy_sword.json")));
        assertTrue(Files.exists(dataMythic.resolve("mighty_sword.json")));
        assertTrue(Files.exists(assetMythic.resolve("mighty_sword.json")));
        assertTrue(Files.exists(dataMythic.resolve("dark_sword.json")));
        assertTrue(Files.exists(assetMythic.resolve("dark_sword.json")));

        String mightyContent = Files.readString(dataMythic.resolve("mighty_sword.json"));
        assertTrue(mightyContent.contains("Skeleton King"));
        assertTrue(mightyContent.contains("4 Bone Blocks"));
        assertTrue(mightyContent.contains("Skeleton Crown"));
        assertTrue(mightyContent.contains("Shellbust Stab"));

        String darkContent = Files.readString(dataMythic.resolve("dark_sword.json"));
        assertTrue(darkContent.contains("The Carver"));
        assertTrue(darkContent.contains("Sculk Catalyst"));
        assertTrue(darkContent.contains("Cursed Cleaver"));
        assertTrue(darkContent.contains("30 XP Levels"));
        assertTrue(darkContent.contains("Night Sword"));
    }

    @Test
    void verifiesMagiciteSummonsRenamingAndBoomSticksRemoved() throws Exception {
        assertTrue(Files.exists(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/categories/magicite_summons.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/categories/magicite_summons.json")));
        assertFalse(Files.exists(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/categories/forbidden_arsenal.json")));
        assertFalse(Files.exists(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/categories/forbidden_arsenal.json")));

        assertFalse(Files.exists(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/magicite_summons/boom_sticks.json")));
        assertFalse(Files.exists(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/magicite_summons/boom_sticks.json")));
        assertFalse(Files.exists(Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book/en_us/entries/forbidden_arsenal")));
        assertFalse(Files.exists(Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book/en_us/entries/forbidden_arsenal")));
    }
}
