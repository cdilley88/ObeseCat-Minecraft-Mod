package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class JRobertPacoheimerItemTest {
    @Test
    void pacoheimerHasDramaticSuspenseDelayAndCooldown() throws Exception {
        Field delayField = JRobertPacoheimerItem.class.getDeclaredField("BARK_TO_EXPLOSION_DELAY_TICKS");
        delayField.setAccessible(true);
        int delay = (int) delayField.get(null);
        assertEquals(20, delay, "Barkkenheimer should have a 1-second (20 ticks) suspense delay between bark and detonation");

        Field cooldownField = JRobertPacoheimerItem.class.getDeclaredField("COOLDOWN_TICKS");
        cooldownField.setAccessible(true);
        int cooldown = (int) cooldownField.get(null);
        assertEquals(100, cooldown, "Barkkenheimer should have a 5-second (100 ticks) anti-spam cooldown");
    }

    @Test
    void pacoheimerMetadataAndModelRegistered() throws Exception {
        String langFile = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        assertTrue(langFile.contains("\"item.obesecat.j_robert_pacoheimer\": \"J. Paco Barkkenheimer\""));

        String modelFile = Files.readString(Path.of("src/main/resources/assets/obesecat/models/item/j_robert_pacoheimer.json"));
        assertTrue(modelFile.contains("\"obesecat:item/barkkenheimer\""));
    }
}
