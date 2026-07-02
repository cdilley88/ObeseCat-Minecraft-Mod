package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class HellhoundPacoItemTest {
    @Test
    void hellhoundPacoUsesSpicyBarksCaption() throws Exception {
        String langFile = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));

        assertTrue(
                langFile.contains("\"item.obesecat.hellhound_paco.caption\": \"Spicy Barks\""),
                "Hellhound Paco should advertise the Spicy Barks caption"
        );
    }
}
