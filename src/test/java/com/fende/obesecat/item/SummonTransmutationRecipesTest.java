package com.fende.obesecat.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SummonTransmutationRecipesTest {
    @Test
    void summonRecipesUseSharedExpensiveIngredientsAndTheirOwnCatalysts() throws Exception {
        assertRecipe("aequitas", "minecraft:soul_torch");
        assertRecipe("praxis", "minecraft:snow_block");
        assertRecipe("paradox", "minecraft:ender_pearl");
        assertRecipe("veritas", "minecraft:glowstone");
    }

    private static void assertRecipe(String summon, String catalyst) throws Exception {
        String recipe = Files.readString(Path.of(
                "src/main/resources/data/obesecat/recipe/" + summon + "_transmutation.json"));
        assertTrue(recipe.contains("\"item\": \"minecraft:nether_star\""));
        assertTrue(recipe.contains("\"item\": \"obesecat:overdrive_powder\""));
        assertTrue(recipe.contains("\"item\": \"" + catalyst + "\""));
        assertTrue(recipe.contains("\"id\": \"obesecat:" + summon + "\""));
    }
}
