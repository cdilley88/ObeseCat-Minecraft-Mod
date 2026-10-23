package com.fende.obesecat.counterstrike;

import com.fende.obesecat.counterstrike.block.entity.FissionTurbineCoreBlockEntity;
import com.fende.obesecat.counterstrike.deploy.StructureBounds;
import com.fende.obesecat.counterstrike.power.BasePowerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FissionTurbineTest {

    @Test
    @DisplayName("Verifies 4x5x4 dimensions: StructureBounds totals 80 blocks (4 wide x 4 high x 5 deep)")
    void testTurbineBounds() {
        StructureBounds bounds = StructureBounds.fissionTurbine();
        assertEquals(4, bounds.maxX() - bounds.minX() + 1, "Width must be 4 blocks (-1 to 2)");
        assertEquals(4, bounds.maxY() - bounds.minY() + 1, "Height must be 4 blocks (-1 to 2)");
        assertEquals(5, bounds.maxZ() - bounds.minZ() + 1, "Depth must be 5 blocks (0 to 4)");
        assertEquals(80, bounds.totalBlocks(), "Total volume must be 80 blocks (smaller than ConYard 150)");
    }

    @Test
    @DisplayName("Verifies Turbine walk-in entrance, reactor rod, interior console, and 4-way rotation math")
    void testTurbinePositioningAndRotationMath() {
        BlockPos origin = new BlockPos(200, 64, 200);

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // Door entrance is at u=0, v=1, w=0 (originPos)
            BlockPos doorEntrance = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 0);
            assertEquals(origin, doorEntrance, "Door entrance must match originPos at ground level");

            // Floor layer replaces ground beneath entrance
            BlockPos floorUnderDoor = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, 0, 0, 0);
            assertEquals(origin.below(), floorUnderDoor, "Floor must be at origin.below()");

            // Interior Master Console at u=0, v=1, w=3
            BlockPos corePos = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 3);
            assertEquals(origin.getY(), corePos.getY(), "Turbine console must sit on ground level");

            // Origin position reconstructed from console position
            BlockPos reconstructedOrigin = FissionTurbineCoreBlockEntity.getOriginPos(corePos, facing);
            assertEquals(origin, reconstructedOrigin, "getOriginPos must accurately reconstruct entrance from corePos");

            // Reactor rod at u=1, v=1, w=2
            BlockPos rodPos = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, 1, 1, 2);
            assertNotEquals(corePos, rodPos, "Reactor rod must be distinct from console");

            // Roof exhaust vent at u=1, v=3, w=2
            BlockPos ventPos = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, 1, 3, 2);
            assertEquals(rodPos.getX(), ventPos.getX());
            assertEquals(rodPos.getZ(), ventPos.getZ());
            assertEquals(origin.getY() + 2, ventPos.getY(), "Vent must be on the roof above the reactor rod");

            // Structure width is 4 blocks (-1 to +2)
            BlockPos leftWall = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, -1, 1, 0);
            BlockPos rightWall = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, 2, 1, 0);
            int span = Math.abs(rightWall.getX() - leftWall.getX()) + Math.abs(rightWall.getZ() - leftWall.getZ()) + 1;
            assertEquals(4, span, "Turbine front wall must span 4 blocks wide");

            // Structure depth is 5 blocks (w from 0 to 4)
            BlockPos backWall = FissionTurbineCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 4);
            int depth = Math.abs(backWall.getX() - origin.getX()) + Math.abs(backWall.getZ() - origin.getZ()) + 1;
            assertEquals(5, depth, "Turbine must extend 5 blocks deep");
        }
    }

    @Test
    @DisplayName("Verifies power ratings: Turbine provides +200 GW, Construction Yard consumes 25 GW")
    void testPowerRatings() {
        assertEquals(200, BasePowerManager.TURBINE_OUTPUT, "Turbine must output +200 GW");
        assertEquals(25, BasePowerManager.CONSTRUCTION_YARD_DRAIN, "Construction Yard must consume 25 GW");
    }

    @Test
    @DisplayName("Verifies Fission Turbine and Construction Yard source files, interfaces, and methods")
    void testTurbineSourceCode() throws Exception {
        Path turbineCoreFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/FissionTurbineCoreBlockEntity.java");
        Path turbineCrateFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/FissionTurbineCrateBlock.java");
        Path turbineReactorFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/TurbineReactorCoreBlock.java");
        Path conYardCoreFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/ConstructionYardCoreBlockEntity.java");
        Path conYardScreenFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/client/ConstructionYardScreen.java");

        assertTrue(Files.exists(turbineCoreFile), "FissionTurbineCoreBlockEntity must exist");
        assertTrue(Files.exists(turbineCrateFile), "FissionTurbineCrateBlock must exist");
        assertTrue(Files.exists(turbineReactorFile), "TurbineReactorCoreBlock must exist");
        assertTrue(Files.exists(conYardCoreFile), "ConstructionYardCoreBlockEntity must exist");
        assertTrue(Files.exists(conYardScreenFile), "ConstructionYardScreen must exist");

        String turbineContent = Files.readString(turbineCoreFile);
        assertTrue(turbineContent.contains("implements IDeployedCore, MenuProvider"), "Turbine must implement IDeployedCore and MenuProvider");
        assertTrue(turbineContent.contains("registerGenerator"), "Turbine must register generator on BasePowerManager");
        assertTrue(turbineContent.contains("unregisterGenerator"), "Turbine must unregister generator on BasePowerManager");
        assertTrue(turbineContent.contains("rotateStructure"), "Turbine must implement rotateStructure");
        assertTrue(turbineContent.contains("packUp"), "Turbine must implement packUp");
        assertTrue(turbineContent.contains("tabbyrium_shard"), "Turbine must support Tabbyrium Shards");
        assertTrue(turbineContent.contains("tabbyrium_crystal"), "Turbine must support Tabbyrium Crystals");
        assertTrue(turbineContent.contains("tabbyrium_block"), "Turbine must support Tabbyrium Blocks");

        String conYardContent = Files.readString(conYardCoreFile);
        assertTrue(conYardContent.contains("registerConsumer"), "ConYard must register as consumer on BasePowerManager");
        assertTrue(conYardContent.contains("startConstruction"), "ConYard must implement startConstruction");
        assertTrue(conYardContent.contains("fission_turbine"), "ConYard must support fission_turbine recipe");

        String screenContent = Files.readString(conYardScreenFile);
        assertTrue(screenContent.contains("Turbine"), "ConYard screen must have Turbine button");
        assertTrue(screenContent.contains("Hopper Inputs"), "ConYard screen must display Hopper Inputs label");
    }

    @Test
    @DisplayName("Verifies all Phase 3 assets, textures, models, and localization entries exist")
    void testPhase3Assets() throws Exception {
        String[] assets = {
                "src/main/resources/assets/obesecat_counterstrike/blockstates/fission_turbine_crate.json",
                "src/main/resources/assets/obesecat_counterstrike/blockstates/fission_turbine_core.json",
                "src/main/resources/assets/obesecat_counterstrike/blockstates/turbine_reactor_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/fission_turbine_crate.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/fission_turbine_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/turbine_reactor_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/fission_turbine_crate.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/fission_turbine_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/turbine_reactor_core.json",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/fission_turbine_crate.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/fission_turbine_core.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/turbine_reactor_core.png"
        };

        for (String path : assets) {
            assertTrue(Files.exists(Path.of(path)), "Missing asset: " + path);
        }

        String langContent = Files.readString(Path.of("src/main/resources/assets/obesecat_counterstrike/lang/en_us.json"));
        assertTrue(langContent.contains("Tabbyrium Fission Turbine Crate"));
        assertTrue(langContent.contains("Fission Turbine Master Console"));
        assertTrue(langContent.contains("Tabbyrium Fission Core"));
    }
}
