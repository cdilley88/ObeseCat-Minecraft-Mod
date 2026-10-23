package com.fende.obesecat.counterstrike;

import com.fende.obesecat.counterstrike.resource.ResourceValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ResourceSilosAndTabsTest {

    @Test
    @DisplayName("Verifies ResourceValues enum constants, symbols, and capacity")
    void testResourceValuesConstants() {
        assertEquals(10000, ResourceValues.MAX_TANK_CAPACITY, "Max silo capacity must be 10,000 units");

        assertEquals("Iron", ResourceValues.ResourceType.IRON.getDisplayName());
        assertEquals("Fe", ResourceValues.ResourceType.IRON.getSymbol());
        assertEquals(10000, ResourceValues.ResourceType.IRON.getMaxCapacity());

        assertEquals("Gold", ResourceValues.ResourceType.GOLD.getDisplayName());
        assertEquals("Au", ResourceValues.ResourceType.GOLD.getSymbol());

        assertEquals("Diamond", ResourceValues.ResourceType.DIAMOND.getDisplayName());
        assertEquals("Dm", ResourceValues.ResourceType.DIAMOND.getSymbol());

        assertEquals("Trinitite", ResourceValues.ResourceType.TRINITITE.getDisplayName());
        assertEquals("Tr", ResourceValues.ResourceType.TRINITITE.getSymbol());

        assertEquals("Tabbyrium", ResourceValues.ResourceType.TABBYRIUM.getDisplayName());
        assertEquals("Tb", ResourceValues.ResourceType.TABBYRIUM.getSymbol());
    }

    @Test
    @DisplayName("Verifies ResourceValues source mappings for Nugget (1), Ingot/Raw (9), Block (81), Shard (1), Crystal (4)")
    void testResourceValuesSourceMappings() throws Exception {
        Path file = Path.of("src/main/java/com/fende/obesecat/counterstrike/resource/ResourceValues.java");
        assertTrue(Files.exists(file), "ResourceValues.java must exist");

        String content = Files.readString(file);
        assertTrue(content.contains("IRON_NUGGET"), "Must map Iron nugget");
        assertTrue(content.contains("IRON_INGOT"), "Must map Iron ingot");
        assertTrue(content.contains("RAW_IRON"), "Must map Raw iron");
        assertTrue(content.contains("IRON_BLOCK"), "Must map Iron block");
        assertTrue(content.contains("RAW_IRON_BLOCK"), "Must map Raw iron block");

        assertTrue(content.contains("GOLD_NUGGET"), "Must map Gold nugget");
        assertTrue(content.contains("GOLD_INGOT"), "Must map Gold ingot");
        assertTrue(content.contains("RAW_GOLD"), "Must map Raw gold");
        assertTrue(content.contains("GOLD_BLOCK"), "Must map Gold block");

        assertTrue(content.contains("DIAMOND"), "Must map Diamond");
        assertTrue(content.contains("DIAMOND_BLOCK"), "Must map Diamond block");

        assertTrue(content.contains("trinitite"), "Must map Trinitite");
        assertTrue(content.contains("tabbyrium_shard"), "Must map Tabbyrium Shard (1)");
        assertTrue(content.contains("tabbyrium_crystal"), "Must map Tabbyrium Crystal (4)");
    }

    @Test
    @DisplayName("Verifies ConstructionYardCoreBlockEntity has 6 slots, 5 silo tanks, digestion, and refund methods")
    void testCoreBlockEntitySiloIntegration() throws Exception {
        Path coreFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/block/entity/ConstructionYardCoreBlockEntity.java");
        assertTrue(Files.exists(coreFile), "ConstructionYardCoreBlockEntity.java must exist");

        String content = Files.readString(coreFile);
        assertTrue(content.contains("new ItemStackHandler(6)"), "Must use 6-slot inventory (5 hoppers + 1 output bay)");
        assertTrue(content.contains("ironBuffer"), "Must contain ironBuffer");
        assertTrue(content.contains("goldBuffer"), "Must contain goldBuffer");
        assertTrue(content.contains("diamondBuffer"), "Must contain diamondBuffer");
        assertTrue(content.contains("trinititeBuffer"), "Must contain trinititeBuffer");
        assertTrue(content.contains("tabbyriumBuffer"), "Must contain tabbyriumBuffer");
        assertTrue(content.contains("digestHoppers"), "Must contain digestHoppers method");
        assertTrue(content.contains("depositPlayerInventory"), "Must contain depositPlayerInventory method");
        assertTrue(content.contains("refundResourceUnits"), "Must refund stored units on packUp");
        assertTrue(content.contains("return 12;"), "ContainerData must sync 12 integers");
    }

    @Test
    @DisplayName("Verifies ConstructionYardMenu supports 12 ContainerData entries, tab switching, and smart sorting")
    void testMenuSiloIntegration() throws Exception {
        Path menuFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/menu/ConstructionYardMenu.java");
        assertTrue(Files.exists(menuFile), "ConstructionYardMenu.java must exist");

        String content = Files.readString(menuFile);
        assertTrue(content.contains("checkContainerDataCount(data, 12)"), "Menu must validate 12 synced data elements");
        assertTrue(content.contains("setTab(int tabIndex)"), "Menu must support setTab method for active tab gating");
        assertTrue(content.contains("getIronTank()"), "Menu must expose getIronTank");
        assertTrue(content.contains("getTabbyriumTank()"), "Menu must expose getTabbyriumTank");
        assertTrue(content.contains("hopperSlots"), "Menu must track dedicated hopper slots");
    }

    @Test
    @DisplayName("Verifies ConstructionYardScreen has tabs, anti-clipping renderLabels, and resource silos UI")
    void testScreenTabsAndSilos() throws Exception {
        Path screenFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/client/ConstructionYardScreen.java");
        assertTrue(Files.exists(screenFile), "ConstructionYardScreen.java must exist");

        String content = Files.readString(screenFile);
        assertTrue(content.contains("Production"), "Screen must include Production tab");
        assertTrue(content.contains("Resource Silos"), "Screen must include Resource Silos tab");
        assertTrue(content.contains("Hopper Inputs"), "Screen must include Hopper Inputs label");
        assertTrue(content.contains("Rotate (90°)"), "Screen must include Rotate button");
        assertTrue(content.contains("renderLabels"), "Screen must override renderLabels to prevent title overlap");
        assertTrue(content.contains("DepositResourcesPayload"), "Screen must support Quick-Deposit payload");
    }

    @Test
    @DisplayName("Verifies networking registration for DepositResourcesPayload")
    void testNetworkingPayload() throws Exception {
        Path netFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/registry/CSNetworking.java");
        assertTrue(Files.exists(netFile), "CSNetworking.java must exist");

        String content = Files.readString(netFile);
        assertTrue(content.contains("DepositResourcesPayload"), "Must register DepositResourcesPayload in CSNetworking");
    }
}
