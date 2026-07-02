package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class StasisSwordItemTest {
    @Test
    void stasisSwordUsesTheExpectedCaptionAndDiamondSwordModel() throws Exception {
        String langFile = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        String modelFile = Files.readString(Path.of("src/main/resources/assets/obesecat/models/item/stasis_sword.json"));

        assertTrue(langFile.contains("\"item.obesecat.stasis_sword.caption\": \"Life is short... Bury!\""));
        assertTrue(modelFile.contains("\"obesecat:item/holysword\""));
    }
}
