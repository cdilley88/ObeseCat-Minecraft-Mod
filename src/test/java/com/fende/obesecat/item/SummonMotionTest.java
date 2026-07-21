package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SummonMotionTest {
    @Test
    void everySummonUsesADistinctArtworkMotionPattern() throws Exception {
        String paradox = Files.readString(Path.of(
                "src/main/java/com/fende/obesecat/world/ParadoxSummonManager.java"));
        String veritas = Files.readString(Path.of(
                "src/main/java/com/fende/obesecat/world/VeritasSummonManager.java"));
        String praxis = Files.readString(Path.of(
                "src/main/java/com/fende/obesecat/world/PraxisSummonManager.java"));
        String aequitas = Files.readString(Path.of(
                "src/main/java/com/fende/obesecat/world/AequitasSummonManager.java"));

        assertTrue(paradox.contains("animateRealityBreak"));
        assertTrue(veritas.contains("animateSanctifiedOrbit"));
        assertTrue(veritas.contains("Math.sin(phase * 2.0D)"));
        assertTrue(praxis.contains("animateFrozenDescent"));
        assertTrue(praxis.contains("Math.pow(1.0D - descent, 3.0D)"));
        assertTrue(aequitas.contains("animateStormTriad"));
        assertTrue(aequitas.contains("List<Display.ItemDisplay> images"));
        assertTrue(aequitas.contains("images.size()"));
    }
}
