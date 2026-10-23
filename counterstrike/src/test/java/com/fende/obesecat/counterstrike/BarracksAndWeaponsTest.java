package com.fende.obesecat.counterstrike;

import com.fende.obesecat.counterstrike.block.entity.BarracksCoreBlockEntity;
import com.fende.obesecat.counterstrike.deploy.StructureBounds;
import com.fende.obesecat.counterstrike.item.*;
import com.fende.obesecat.counterstrike.power.BasePowerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BarracksAndWeaponsTest {

    @Test
    @DisplayName("Verifies 5x5x4 dimensions: StructureBounds totals 100 blocks (5 wide x 4 high x 5 deep)")
    void testBarracksBounds() {
        StructureBounds bounds = StructureBounds.barracks();
        assertEquals(5, bounds.maxX() - bounds.minX() + 1, "Width must be 5 blocks (-2 to 2)");
        assertEquals(4, bounds.maxY() - bounds.minY() + 1, "Height must be 4 blocks (-1 to 2)");
        assertEquals(5, bounds.maxZ() - bounds.minZ() + 1, "Depth must be 5 blocks (0 to 4)");
        assertEquals(100, bounds.totalBlocks(), "Total volume must be 100 blocks");
    }

    @Test
    @DisplayName("Verifies Barracks walk-in entrance, armory lockers, interior console, and 4-way rotation math")
    void testBarracksPositioningAndRotationMath() {
        BlockPos origin = new BlockPos(300, 64, 300);

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // Door entrance is at u=0, v=1, w=0 (originPos)
            BlockPos doorEntrance = BarracksCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 0);
            assertEquals(origin, doorEntrance, "Door entrance must match originPos at ground level");

            // Floor layer replaces ground beneath entrance
            BlockPos floorUnderDoor = BarracksCoreBlockEntity.getRelativePos(origin, facing, 0, 0, 0);
            assertEquals(origin.below(), floorUnderDoor, "Floor must be at origin.below()");

            // Interior Master Console at u=0, v=1, w=3
            BlockPos corePos = BarracksCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 3);
            assertEquals(origin.getY(), corePos.getY(), "Barracks console must sit on ground level");

            // Origin position reconstructed from console position
            BlockPos reconstructedOrigin = BarracksCoreBlockEntity.getOriginPos(corePos, facing);
            assertEquals(origin, reconstructedOrigin, "getOriginPos must accurately reconstruct entrance from corePos");

            // Left armory lockers at u=-1, v=1, w=2..3
            BlockPos leftLocker = BarracksCoreBlockEntity.getRelativePos(origin, facing, -1, 1, 2);
            assertNotEquals(corePos, leftLocker);

            // Right armory lockers at u=1, v=1, w=2..3
            BlockPos rightLocker = BarracksCoreBlockEntity.getRelativePos(origin, facing, 1, 1, 2);
            assertNotEquals(corePos, rightLocker);
            assertNotEquals(leftLocker, rightLocker);

            // Structure width is 5 blocks (-2 to +2)
            BlockPos leftWall = BarracksCoreBlockEntity.getRelativePos(origin, facing, -2, 1, 0);
            BlockPos rightWall = BarracksCoreBlockEntity.getRelativePos(origin, facing, 2, 1, 0);
            int span = Math.abs(rightWall.getX() - leftWall.getX()) + Math.abs(rightWall.getZ() - leftWall.getZ()) + 1;
            assertEquals(5, span, "Barracks front wall must span 5 blocks wide");

            // Structure depth is 5 blocks (w from 0 to 4)
            BlockPos backWall = BarracksCoreBlockEntity.getRelativePos(origin, facing, 0, 1, 4);
            int depth = Math.abs(backWall.getX() - origin.getX()) + Math.abs(backWall.getZ() - origin.getZ()) + 1;
            assertEquals(5, depth, "Barracks must extend 5 blocks deep");
        }
    }

    @Test
    @DisplayName("Verifies power ratings: Barracks consumes 20 GW base power")
    void testPowerDrain() {
        assertEquals(20, BasePowerManager.BARRACKS_DRAIN, "Barracks must consume 20 GW");
    }

    @Test
    @DisplayName("Verifies weapon magazine capacities, fire rates, and reload cooldown values")
    void testWeaponConstants() {
        assertEquals(24, AutomaticRifleItem.MAG_SIZE, "Auto rifle must have 24-round magazine");
        assertEquals(40, AutomaticRifleItem.RELOAD_TICKS, "Auto rifle must have 40-tick reload");

        assertEquals(60, RocketLauncherItem.RELOAD_TICKS, "Rocket launcher must have 60-tick reload");

        assertEquals(20, FragGrenadeItem.THROW_COOLDOWN, "Frag grenade must have 20-tick cooldown");

        assertEquals(40, FlamethrowerItem.CANISTER_CAPACITY, "Flamethrower must have 40-unit canister");
        assertEquals(60, FlamethrowerItem.RECHARGE_TICKS, "Flamethrower must have 60-tick recharge");

        assertEquals(30, CommandoRifleItem.MAG_SIZE, "Commando rifle must have 30-round magazine");
        assertEquals(45, CommandoRifleItem.RELOAD_TICKS, "Commando rifle must have 45-tick reload");
        assertEquals(70, CommandoRifleItem.C4_COOLDOWN_TICKS, "Commando rifle must have 70-tick C-4 cooldown");
    }

    @Test
    @DisplayName("Verifies Barracks source code, recipes, and UI integration")
    void testBarracksSourceCode() throws Exception {
        Path barracksCoreFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/BarracksCoreBlockEntity.java");
        Path barracksCrateFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/BarracksCrateBlock.java");
        Path armoryLockerFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/ArmoryLockerBlock.java");
        Path barracksScreenFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/client/BarracksScreen.java");
        Path conYardFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/ConstructionYardCoreBlockEntity.java");

        assertTrue(Files.exists(barracksCoreFile), "BarracksCoreBlockEntity must exist");
        assertTrue(Files.exists(barracksCrateFile), "BarracksCrateBlock must exist");
        assertTrue(Files.exists(armoryLockerFile), "ArmoryLockerBlock must exist");
        assertTrue(Files.exists(barracksScreenFile), "BarracksScreen must exist");

        String barracksContent = Files.readString(barracksCoreFile);
        assertTrue(barracksContent.contains("implements IDeployedCore, MenuProvider"), "Barracks must implement IDeployedCore and MenuProvider");
        assertTrue(barracksContent.contains("automatic_rifle"), "Barracks must craft automatic rifle");
        assertTrue(barracksContent.contains("rocket_launcher"), "Barracks must craft rocket launcher");
        assertTrue(barracksContent.contains("frag_grenade"), "Barracks must craft frag grenade");
        assertTrue(barracksContent.contains("flamethrower"), "Barracks must craft flamethrower");
        assertTrue(barracksContent.contains("commando_rifle"), "Barracks must craft commando rifle");
        assertTrue(barracksContent.contains("BARRACKS_DRAIN"), "Barracks must register BARRACKS_DRAIN");

        String conYardContent = Files.readString(conYardFile);
        assertTrue(conYardContent.contains("\"barracks\""), "Construction Yard must have recipe for barracks");

        String screenContent = Files.readString(barracksScreenFile);
        assertTrue(screenContent.contains("EVA INFANTRY PROCUREMENT"), "Screen must have procurement header");
        assertTrue(screenContent.contains("Commando"), "Screen must have Commando button");
    }

    @Test
    @DisplayName("Verifies all Barracks and infantry weapon textures, models, and localizations")
    void testAssets() throws Exception {
        String[] assets = {
                "src/main/resources/assets/obesecat_counterstrike/blockstates/barracks_crate.json",
                "src/main/resources/assets/obesecat_counterstrike/blockstates/barracks_core.json",
                "src/main/resources/assets/obesecat_counterstrike/blockstates/armory_locker.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/barracks_crate.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/barracks_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/block/armory_locker.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/barracks_crate.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/barracks_core.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/armory_locker.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/automatic_rifle.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/rocket_launcher.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/frag_grenade.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/flamethrower.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/commando_rifle.json",
                "src/main/resources/assets/obesecat_counterstrike/models/item/c4_charge.json",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/barracks_crate.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/barracks_core.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/block/armory_locker.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/item/automatic_rifle.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/item/rocket_launcher.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/item/frag_grenade.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/item/flamethrower.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/item/commando_rifle.png",
                "src/main/resources/assets/obesecat_counterstrike/textures/item/c4_charge.png"
        };

        for (String path : assets) {
            assertTrue(Files.exists(Path.of(path)), "Missing asset: " + path);
        }

        String langContent = Files.readString(Path.of("src/main/resources/assets/obesecat_counterstrike/lang/en_us.json"));
        assertTrue(langContent.contains("Infantry Barracks Crate"));
        assertTrue(langContent.contains("Barracks Procurement Console"));
        assertTrue(langContent.contains("Armory Weapon Locker"));
        assertTrue(langContent.contains("Automatic Rifle"));
        assertTrue(langContent.contains("Rocket Launcher"));
        assertTrue(langContent.contains("Frag Grenade"));
        assertTrue(langContent.contains("Flamethrower"));
        assertTrue(langContent.contains("Commando Assault Rifle"));
        assertTrue(langContent.contains("C-4 Charge"));
    }
}
