package com.fende.obesecat.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatchouliBookTest {
    private static final Path DATA_ROOT = Path.of("src/main/resources/data/obesecat/patchouli_books/guide_book");
    private static final Path ASSETS_ROOT = Path.of("src/main/resources/assets/obesecat/patchouli_books/guide_book");

    @Test
    public void testBookJsonIsValid() throws IOException {
        Path bookJson = DATA_ROOT.resolve("book.json");
        assertTrue(Files.exists(bookJson), "book.json must exist in data");
        JsonObject obj = JsonParser.parseString(Files.readString(bookJson)).getAsJsonObject();
        assertTrue(obj.has("name"));
        assertTrue(obj.has("landing_text"));

        Path assetBookJson = ASSETS_ROOT.resolve("book.json");
        assertTrue(Files.exists(assetBookJson), "book.json must exist in assets");
    }

    @Test
    public void testCategoriesAreValid() throws IOException {
        Path categoriesDir = ASSETS_ROOT.resolve("en_us/categories");
        assertTrue(Files.isDirectory(categoriesDir), "categories directory must exist in assets");

        List<String> expectedCategories = List.of(
                "manhattan_project.json",
                "bovine_dimension.json",
                "mythic_blades.json",
                "companion_entities.json",
                "forbidden_arsenal.json"
        );

        for (String catFile : expectedCategories) {
            Path file = categoriesDir.resolve(catFile);
            assertTrue(Files.exists(file), "Category file " + catFile + " must exist");
            JsonObject obj = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            assertTrue(obj.has("name"));
            assertTrue(obj.has("icon"));
        }
    }

    @Test
    public void testEntriesAreValid() throws IOException {
        Path entriesDir = ASSETS_ROOT.resolve("en_us/entries");
        assertTrue(Files.isDirectory(entriesDir), "entries directory must exist in assets");

        try (var stream = Files.walk(entriesDir)) {
            List<Path> jsonFiles = stream.filter(p -> p.toString().endsWith(".json")).toList();
            assertFalse(jsonFiles.isEmpty(), "Entries must not be empty");

            for (Path entryFile : jsonFiles) {
                JsonObject obj = JsonParser.parseString(Files.readString(entryFile)).getAsJsonObject();
                assertTrue(obj.has("name"), entryFile + " missing 'name'");
                assertTrue(obj.has("category"), entryFile + " missing 'category'");
                assertTrue(obj.has("pages"), entryFile + " missing 'pages'");
                assertTrue(obj.getAsJsonArray("pages").size() > 0, entryFile + " has empty pages");
            }
        }
    }
}
