package com.fende.obesecat.counterstrike;

import com.fende.obesecat.counterstrike.deploy.StructureBounds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CommandCursorTest {

    @Test
    @DisplayName("Verifies StructureBounds volume and coordinate logic")
    void testStructureBounds() {
        // 5 wide, 4 high, 4 deep centered bounds
        StructureBounds bounds = StructureBounds.centered(5, 4, 4);
        assertEquals(-2, bounds.minX());
        assertEquals(2, bounds.maxX());
        assertEquals(0, bounds.minY());
        assertEquals(3, bounds.maxY());
        assertEquals(-2, bounds.minZ());
        assertEquals(1, bounds.maxZ());
        assertEquals(5 * 4 * 4, bounds.totalBlocks(), "Total volume should be 80 blocks");
    }

    @Test
    @DisplayName("Verifies CommandCursorItem class source requirements")
    void testCommandCursorItemSource() throws Exception {
        Path itemFile = Path.of("src/main/java/com/fende/obesecat/counterstrike/item/CommandCursorItem.java");
        assertTrue(Files.exists(itemFile), "CommandCursorItem.java must exist");

        String content = Files.readString(itemFile);
        assertTrue(content.contains("stacksTo(1)"), "Command Cursor must not stack");
        assertTrue(content.contains("Rarity.RARE"), "Command Cursor should be RARE rarity");
        assertTrue(content.contains("fireResistant()"), "Command Cursor should be fire resistant");
        assertTrue(content.contains("EVA TACTICAL COMMAND CURSOR"), "Hover text must include EVA title");
        assertTrue(content.contains("Battlefield Control Online"), "Hover text must include flavor text");
        assertTrue(content.contains("IDeployableStructure"), "Must interact with IDeployableStructure");
        assertTrue(content.contains("IDeployedCore"), "Must interact with IDeployedCore");
    }

    @Test
    @DisplayName("Verifies Command Cursor asset files and registrations")
    void testCommandCursorAssets() throws Exception {
        Path langPath = Path.of("src/main/resources/assets/obesecat_counterstrike/lang/en_us.json");
        Path modelPath = Path.of("src/main/resources/assets/obesecat_counterstrike/models/item/command_cursor.json");
        Path texturePath = Path.of("src/main/resources/assets/obesecat_counterstrike/textures/item/command_cursor.png");

        assertTrue(Files.exists(langPath), "lang/en_us.json must exist");
        assertTrue(Files.exists(modelPath), "models/item/command_cursor.json must exist");
        assertTrue(Files.exists(texturePath), "textures/item/command_cursor.png must exist");

        String langContent = Files.readString(langPath);
        assertTrue(langContent.contains("\"item.obesecat_counterstrike.command_cursor\": \"Command Cursor\""));
        assertTrue(langContent.contains("\"itemGroup.obesecat_counterstrike\": \"ObeseCat: Counterstrike\""));

        String modelContent = Files.readString(modelPath);
        assertTrue(modelContent.contains("\"obesecat_counterstrike:item/command_cursor\""));
    }

    @Test
    @DisplayName("Verifies neoforge.mods.toml declares mandatory dependency on obesecat")
    void testModDependencyDeclaration() throws Exception {
        Path tomlPath = Path.of("src/main/resources/META-INF/neoforge.mods.toml");
        assertTrue(Files.exists(tomlPath), "neoforge.mods.toml must exist");

        String content = Files.readString(tomlPath);
        assertTrue(content.contains("modId=\"obesecat_counterstrike\""), "Must define obesecat_counterstrike modId");
        assertTrue(content.contains("modId=\"obesecat\""), "Must declare dependency on obesecat base mod");
        assertTrue(content.contains("type=\"required\""), "Base mod dependency must be required");
    }
}
