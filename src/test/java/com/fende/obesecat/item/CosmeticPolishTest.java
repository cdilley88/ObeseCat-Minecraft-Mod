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
        assertTrue(lang.contains("\"item.obesecat.downpour_domino.caption\": \"Warm Home\""));
        assertTrue(lang.contains("\"item.obesecat.thunderstorm_domino.caption\": \"Safe Home\""));
        assertFalse(lang.contains("Thankful for Warm Home"));
        assertFalse(lang.contains("Thankful for Safe Home"));
        assertTrue(eggModel.contains("minecraft:item/template_spawn_egg"));

        assertTrue(itemBlock(items, "DAWN_DOMINO", "MIDDAY_DOMINO").contains("Rarity.RARE"));
        assertTrue(itemBlock(items, "MIDDAY_DOMINO", "DUSK_DOMINO").contains("Rarity.RARE"));
        assertTrue(itemBlock(items, "DUSK_DOMINO", "MIDNIGHT_DOMINO").contains("Rarity.RARE"));
        assertTrue(itemBlock(items, "MIDNIGHT_DOMINO", "CALM_DOMINO").contains("Rarity.RARE"));
        assertTrue(itemBlock(items, "CALM_DOMINO", "DOWNPOUR_DOMINO").contains("Rarity.RARE"));
        assertTrue(itemBlock(items, "DOWNPOUR_DOMINO", "THUNDERSTORM_DOMINO").contains("Rarity.RARE"));
        assertTrue(itemBlock(items, "THUNDERSTORM_DOMINO", "EMBER").contains("Rarity.RARE"));
        assertTrue(itemBlock(items, "DOMINO", "DAWN_DOMINO").contains("Rarity.UNCOMMON"));
    }

    private static String itemBlock(String source, String start, String end) {
        return source.substring(source.indexOf(start), source.indexOf(end));
    }
}
