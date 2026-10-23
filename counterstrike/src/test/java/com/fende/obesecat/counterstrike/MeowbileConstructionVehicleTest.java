package com.fende.obesecat.counterstrike;

import com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity;
import com.fende.obesecat.counterstrike.deploy.IDeployableStructure;
import com.fende.obesecat.counterstrike.deploy.IDeployedCore;
import com.fende.obesecat.counterstrike.deploy.StructureBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MeowbileConstructionVehicleTest {

    @Test
    @DisplayName("Verifies MCV block implements IDeployableStructure, facing, and walk-in deployment")
    void testMcvInterfaceAndBounds() throws Exception {
        Path mcvFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/MeowbileConstructionVehicleBlock.java");
        assertTrue(Files.exists(mcvFile), "MeowbileConstructionVehicleBlock.java must exist");

        String content = Files.readString(mcvFile);
        assertTrue(content.contains("implements IDeployableStructure"), "MCV must implement IDeployableStructure");
        assertTrue(content.contains("FACING"), "MCV must have FACING property");
        assertTrue(content.contains("\"Construction Yard\""), "MCV structure name must be 'Construction Yard'");
        assertTrue(content.contains("buildStructure"), "Must delegate to buildStructure for 5x6x5 deployment");
        assertTrue(content.contains("StructureBounds.constructionYard"), "Must use 5x6x5 construction yard bounds");
    }

    @Test
    @DisplayName("Verifies 5x6x5 dimensions: StructureBounds totals 150 blocks (5 wide x 5 high x 6 deep)")
    void testConstructionYardBounds() {
        StructureBounds bounds = StructureBounds.constructionYard();
        assertEquals(5, bounds.maxX() - bounds.minX() + 1, "Width must be 5 blocks (-2 to 2)");
        assertEquals(5, bounds.maxY() - bounds.minY() + 1, "Height must be 5 blocks (-1 to 3)");
        assertEquals(6, bounds.maxZ() - bounds.minZ() + 1, "Depth must be 6 blocks (0 to 5)");
        assertEquals(150, bounds.totalBlocks(), "Total volume must be 150 blocks");
    }

    @Test
    @DisplayName("Verifies walk-in entrance at ground level, interior console positioning, and 4-way rotation math")
    void testCoreLongSidePositioningAndRotationMath() {
        BlockPos origin = new BlockPos(100, 64, 100);

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // Door entrance is at u=0, v=1, w=0 (originPos)
            BlockPos doorEntrance = ConstructionYardCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 0);
            assertEquals(origin, doorEntrance, "Door entrance must match originPos at ground level");

            // Door upper half is directly above entrance
            BlockPos doorUpper = ConstructionYardCoreBlockEntity.getRelativePos(origin, facing, 0, 2, 0);
            assertEquals(origin.above(), doorUpper, "Door upper half must be at origin.above()");

            // Floor layer replaces ground beneath the entrance
            BlockPos floorUnderDoor = ConstructionYardCoreBlockEntity.getRelativePos(origin, facing, 0, 0, 0);
            assertEquals(origin.below(), floorUnderDoor, "Floor must be at origin.below() replacing top ground layer");

            // Master Core console is inside at depth w = 4, lateral u = 0, height v = 1
            BlockPos corePos = ConstructionYardCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 4);
            assertEquals(origin.getY(), corePos.getY(), "Core console must sit on the ground-level walking surface");

            // Origin position reconstructed from Core console and facing
            BlockPos reconstructedOrigin = ConstructionYardCoreBlockEntity.getOriginPos(corePos, facing);
            assertEquals(origin, reconstructedOrigin, "getOriginPos must accurately reconstruct entrance from corePos");

            // Structure width is 5 blocks lateral (-2 to +2)
            BlockPos leftWall = ConstructionYardCoreBlockEntity.getRelativePos(origin, facing, -2, 1, 0);
            BlockPos rightWall = ConstructionYardCoreBlockEntity.getRelativePos(origin, facing, 2, 1, 0);
            int span = Math.abs(rightWall.getX() - leftWall.getX()) + Math.abs(rightWall.getZ() - leftWall.getZ()) + 1;
            assertEquals(5, span, "Front wall must span 5 blocks wide");

            // Structure depth is 6 blocks (w from 0 to 5)
            BlockPos backWall = ConstructionYardCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 5);
            int depth = Math.abs(backWall.getX() - origin.getX()) + Math.abs(backWall.getZ() - origin.getZ()) + 1;
            assertEquals(6, depth, "Structure must extend 6 blocks deep");
        }
    }

    @Test
    @DisplayName("Verifies Construction Yard Core and Hull block properties, pack-up, and rotation logic")
    void testConstructionYardBlocksAndRotation() throws Exception {
        Path coreFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/ConstructionYardCoreBlockEntity.java");
        Path hullFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/ConstructionYardHullBlockEntity.java");
        Path doorEntityFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/PneumaticDoorBlockEntity.java");
        Path doorBlockFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/PneumaticDoorBlock.java");
        Path screenFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/client/ConstructionYardScreen.java");
        Path payloadFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/network/RotateStructurePayload.java");

        assertTrue(Files.exists(coreFile), "ConstructionYardCoreBlockEntity must exist");
        assertTrue(Files.exists(hullFile), "ConstructionYardHullBlockEntity must exist");
        assertTrue(Files.exists(doorEntityFile), "PneumaticDoorBlockEntity must exist");
        assertTrue(Files.exists(doorBlockFile), "PneumaticDoorBlock must exist");
        assertTrue(Files.exists(screenFile), "ConstructionYardScreen must exist");
        assertTrue(Files.exists(payloadFile), "RotateStructurePayload must exist");

        String coreContent = Files.readString(coreFile);
        assertTrue(coreContent.contains("implements IDeployedCore"), "Core entity must implement IDeployedCore");
        assertTrue(coreContent.contains("implements IDeployedCore, MenuProvider") || coreContent.contains("MenuProvider"), "Core must implement MenuProvider");
        assertTrue(coreContent.contains("rotateStructure"), "Core must implement rotateStructure");
        assertTrue(coreContent.contains("packUp"), "Core entity must implement packUp logic");
        assertTrue(coreContent.contains("buildStructure"), "Core must implement buildStructure");
        assertTrue(coreContent.contains("PNEUMATIC_DOOR"), "buildStructure must place Pneumatic Door");

        String doorContent = Files.readString(doorBlockFile);
        assertTrue(doorContent.contains("DoubleBlockHalf"), "Pneumatic door must support double block half");
        assertTrue(doorContent.contains("setCorePos") || doorContent.contains("getCorePos"), "Door must support corePos delegation");

        String screenContent = Files.readString(screenFile);
        assertTrue(screenContent.contains("Rotate (90°)"), "Screen must include Rotate button");
        assertTrue(screenContent.contains("RotateStructurePayload"), "Screen button must send RotateStructurePayload");
    }

    @Test
    @DisplayName("Verifies MCV crafting recipe uses Tabbyrium, Trinitite, and Iron")
    void testMcvRecipeJson() throws Exception {
        Path recipePath = Path.of("src/main/resources/data/obesecat_counterstrike/recipe/meowbile_construction_vehicle.json");
        assertTrue(Files.exists(recipePath), "MCV recipe JSON must exist");

        String recipe = Files.readString(recipePath);
        assertTrue(recipe.contains("obesecat:tabbyrium_crystal"), "Recipe must require Tabbyrium Crystal");
        assertTrue(recipe.contains("obesecat:trinitite"), "Recipe must require Trinitite");
        assertTrue(recipe.contains("minecraft:iron_block"), "Recipe must require Iron Block");
        assertTrue(recipe.contains("minecraft:redstone_block"), "Recipe must require Redstone Block");
        assertTrue(recipe.contains("obesecat_counterstrike:meowbile_construction_vehicle"), "Recipe must output MCV");
    }

    @Test
    @DisplayName("Verifies all Phase 2 assets, textures, and models exist including Pneumatic Door")
    void testPhase2Assets() throws Exception {
        String[] assets = {
                "src/main/resources/assets/obesecat_counterstrike/blockstates/meowbile_construction_vehicle.json",
                "src/main/resources/assets/obesecat_counterstrike/blockstates/construction_yard_core.json",
                "src/main/resources/assets/obesecat_counterstrike/blockstates/construction_yard_hull.json",
                "src/main/resources/assets/obesecat_counterstrike/blockstates/pneumatic_door.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/meowbile_construction_vehicle.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/construction_yard_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/construction_yard_hull.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/pneumatic_door_bottom.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/pneumatic_door_bottom_open.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/pneumatic_door_top.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/pneumatic_door_top_open.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/meowbile_construction_vehicle.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/construction_yard_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/construction_yard_hull.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/pneumatic_door.json",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/meowbile_construction_vehicle.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/construction_yard_core.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/construction_yard_hull.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/pneumatic_door_bottom.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/pneumatic_door_top.png"
        };

        for (String path : assets) {
            assertTrue(Files.exists(Path.of(path)), "Missing asset: " + path);
        }

        String langContent = Files.readString(Path.of("src/main/resources/assets/obesecat_counterstrike/lang/en_us.json"));
        assertTrue(langContent.contains("Meow-bile Construction Vehicle"));
        assertTrue(langContent.contains("Construction Yard Core"));
        assertTrue(langContent.contains("Construction Yard Hull"));
        assertTrue(langContent.contains("Pneumatic Blast Door"));
    }
}
