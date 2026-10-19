package com.fende.obesecat.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FatManAndTabbyriumTest {

    @Test
    void verifiesFatManCriticalMassAndNoEnergySystem() throws Exception {
        String obeseCatFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/entity/ObeseCat.java"));
        String nuclearFoodFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/NuclearCatFoodItem.java"));

        // No Cat Food Energy or IEnergyStorage in ObeseCat
        assertFalse(obeseCatFile.contains("CAT_FOOD_ENERGY"));
        assertFalse(obeseCatFile.contains("IEnergyStorage"));
        assertFalse(obeseCatFile.contains("EnergyStorage"));

        // Reactive meals mechanism for critical mass
        assertTrue(obeseCatFile.contains("REACTIVE_MEALS"));
        assertTrue(obeseCatFile.contains("newMeals >= 3"));
        assertTrue(obeseCatFile.contains("EXPLOSION_COUNTDOWN"));

        // Food meals
        assertTrue(nuclearFoodFile.contains("mealsProvided"));
        assertTrue(nuclearFoodFile.contains("isInstantDetonation"));
    }

    @Test
    void verifiesTabbyriumHazardIsVanillaWither() throws Exception {
        String clusterFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumClusterBlock.java"));
        String crustFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumCrustBlock.java"));

        // Cluster applies vanilla Wither
        assertTrue(clusterFile.contains("MobEffects.WITHER"), "Tabbyrium Cluster must apply vanilla Wither");
        assertTrue(clusterFile.contains("stepOn"), "Tabbyrium Cluster must inflict hazard in stepOn");

        // Crust applies vanilla Wither
        assertTrue(crustFile.contains("MobEffects.WITHER"), "Tabbyrium Crust must apply vanilla Wither");
        assertTrue(crustFile.contains("stepOn"), "Tabbyrium Crust must inflict hazard in stepOn");
    }

    @Test
    void verifiesEasterEggMessages() throws Exception {
        String siloFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumSiloBlock.java"));
        String refineryFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumRefineryBlock.java"));

        assertTrue(siloFile.contains("[EVA] §fWARNING: SILOS NEEDED."));
        assertTrue(refineryFile.contains("[EVA] §fRefinery operational. Establishing battlefield control."));
    }

    @Test
    void verifiesTabbyriumStalkPlanksAndLifecycle() throws Exception {
        String stalkFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumStalkBlock.java"));
        String crustFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumCrustBlock.java"));
        String saplingFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumBlossomSaplingBlock.java"));
        String modBlocksFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModBlocks.java"));
        String modItemsFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModItems.java"));

        // Registration
        assertTrue(modBlocksFile.contains("TABBYRIUM_STALK"));
        assertTrue(modBlocksFile.contains("TABBYRIUM_PLANKS"));
        assertTrue(modItemsFile.contains("TABBYRIUM_STALK"));
        assertTrue(modItemsFile.contains("TABBYRIUM_PLANKS"));

        // Stalk is RotatedPillarBlock with randomTicks to leach ground into crust
        assertTrue(stalkFile.contains("RotatedPillarBlock"));
        assertTrue(stalkFile.contains("randomTicks()"));
        assertTrue(stalkFile.contains("TABBYRIUM_CRUST"));

        // Crust has randomTicks to sprout and mature clusters
        assertTrue(crustFile.contains("randomTicks()"));
        assertTrue(crustFile.contains("TABBYRIUM_CLUSTER"));
        assertTrue(crustFile.contains("setValue(TabbyriumClusterBlock.AGE, 0)"));

        // Sapling grows trunk from Tabbyrium Stalk (3, 4, or 5 blocks tall at random)
        assertTrue(saplingFile.contains("ModBlocks.TABBYRIUM_STALK.get().defaultBlockState()"));
        assertTrue(saplingFile.contains("3 + random.nextInt(3)"));

        // Max 18 crust cap prevents carpet proliferation
        assertTrue(stalkFile.contains("MAX_CRUST_PER_BLOSSOM = 18"));
    }

    @Test
    void verifiesSporePodPulsingAndLight() throws Exception {
        String podFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/block/TabbyriumSporePodBlock.java"));
        assertTrue(podFile.contains("MAX_CRUST_PER_BLOSSOM"));
        Path mcmetaFile = Path.of("src/main/resources/assets/obesecat/textures/block/tabbyrium_spore_pod.png.mcmeta");

        // Light level is maximum (15) like Sea Lantern
        assertTrue(podFile.contains(".lightLevel(state -> 15)"));

        // Animation mcmeta exists with interpolate: true
        assertTrue(Files.exists(mcmetaFile), "tabbyrium_spore_pod.png.mcmeta must exist");
        String mcmetaContent = Files.readString(mcmetaFile);
        assertTrue(mcmetaContent.contains("\"interpolate\": true"));
        assertTrue(mcmetaContent.contains("\"animation\""));
    }

    @Test
    void verifiesCutoutModelsAndRenderType() throws Exception {
        String clientFile = Files.readString(Path.of("src/main/java/com/fende/obesecat/ObeseCatModClient.java"));
        String saplingModel = Files.readString(Path.of("src/main/resources/assets/obesecat/models/block/tabbyrium_blossom_sapling.json"));
        String clusterModel0 = Files.readString(Path.of("src/main/resources/assets/obesecat/models/block/tabbyrium_cluster_stage0.json"));
        String clusterModel3 = Files.readString(Path.of("src/main/resources/assets/obesecat/models/block/tabbyrium_cluster_stage3.json"));

        // Cutout registered in client setup
        assertTrue(clientFile.contains("TABBYRIUM_BLOSSOM_SAPLING.get(), RenderType.cutout()"));
        assertTrue(clientFile.contains("TABBYRIUM_CLUSTER.get(), RenderType.cutout()"));

        // Cutout declared in model JSONs
        assertTrue(saplingModel.contains("\"render_type\": \"minecraft:cutout\""));
        assertTrue(clusterModel0.contains("\"render_type\": \"minecraft:cutout\""));
        assertTrue(clusterModel3.contains("\"render_type\": \"minecraft:cutout\""));
    }
}
