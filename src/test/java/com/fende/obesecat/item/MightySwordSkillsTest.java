package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MightySwordSkillsTest {
    @Test
    void remainingMightySwordSkillsUseCorrectQuotesAndEquipmentSlots() throws Exception {
        String manager = Files.readString(Path.of("src/main/java/com/fende/obesecat/world/MightySwordBreakManager.java"));
        String lang = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));

        assertTrue(manager.contains("BLASTAR_PUNCH(EquipmentSlot.HEAD"));
        assertTrue(manager.contains("HELLCRY_PUNCH(EquipmentSlot.MAINHAND"));
        assertTrue(manager.contains("ICEWOLF_BITE(EquipmentSlot.OFFHAND"));
        assertTrue(manager.contains("EQUIPMENT_DROP_CHANCE = 0.50F"));
        assertTrue(manager.contains("PENDING_METEORS"));
        assertTrue(manager.contains("BLASTAR_PUNCH_GFX"));
        assertTrue(lang.contains("\"item.obesecat.blastar_punch.caption\": \"Curses from all directions!\""));
        assertTrue(lang.contains("\"item.obesecat.hellcry_punch.caption\": \"Demolish weapons with fury!\""));
        assertTrue(lang.contains("\"item.obesecat.icewolf_bite.caption\": \"Disaster cries out to smash all...\""));
    }
}
