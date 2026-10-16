package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CosmeticPolishTest {
    @Test
    void requestedCaptionsRaritiesAndCowKingEggModelArePresent() throws Exception {
        String items = Files.readString(Path.of("src/main/java/com/fende/obesecat/registry/ModItems.java"));
        String aequitas = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/AequitasItem.java"));
        String chamber = Files.readString(Path.of("src/main/java/com/fende/obesecat/item/EchoingBlastChamberItem.java"));
        String lang = Files.readString(Path.of("src/main/resources/assets/obesecat/lang/en_us.json"));
        String eggModel = Files.readString(Path.of(
                "src/main/resources/assets/obesecat/models/item/cow_king_spawn_egg.json"));

        assertTrue(aequitas.contains("item.obesecat.aequitas.caption"));
        assertTrue(lang.contains("\"item.obesecat.aequitas.caption\": \"Full Measure\""));
        assertTrue(lang.contains("Echoes of an incredible noise"));
        assertTrue(chamber.contains("line <= 1"));
        assertTrue(eggModel.contains("minecraft:item/template_spawn_egg"));
        assertTrue(itemBlock(items, "DOMINO", "EMBER").contains("Rarity.UNCOMMON"));
    }

    private static String itemBlock(String source, String start, String end) {
        return source.substring(source.indexOf(start), source.indexOf(end));
    }
}
