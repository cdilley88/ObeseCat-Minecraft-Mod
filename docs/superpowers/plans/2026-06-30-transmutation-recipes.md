# Transmutation Recipes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Use `superpowers:test-driven-development` for every behavior change and `superpowers:verification-before-completion` before claiming completion.

**Goal:** Replace the normal crafting recipe for J. Paco Barkkenheimer with the first data-driven Transmutation Cube recipe: exactly one Paco plus one Oppenheimer's Hat, in any two of the cube's 12 slots and in either order, becomes one J. Paco Barkkenheimer inside the cube.

**Architecture:** Add a custom `obesecat:transmutation` recipe type and serializer backed by JSON. Recipe matching compares exact unordered item/count multisets across all 12 cube slots, so slot positions do not matter and any surplus, missing, or wrong item rejects the recipe. The menu remains server-authoritative; after a successful match it atomically replaces the inputs with the result in the lowest-index occupied input slot and persists the new contents to the cube ItemStack.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge 21.1.228, Mojang codecs/stream codecs, vanilla recipe manager, NeoForge GameTest, JSON data resources.

---

## File Map

**Create:**

- `src/main/java/com/fende/obesecat/recipe/TransmutationIngredient.java` — counted item entry used by the JSON and network codecs.
- `src/main/java/com/fende/obesecat/recipe/TransmutationInput.java` — immutable 12-slot recipe input snapshot.
- `src/main/java/com/fende/obesecat/recipe/TransmutationRecipe.java` — exact unordered matcher, result assembly, and serializer.
- `src/main/java/com/fende/obesecat/registry/ModRecipeTypes.java` — deferred recipe type and serializer registration.
- `src/main/resources/data/obesecat/recipe/j_robert_pacoheimer_transmutation.json` — Paco + Oppenheimer's Hat cube recipe.

**Modify:**

- `src/main/java/com/fende/obesecat/ObeseCatMod.java` — register the recipe type and serializer.
- `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java` — matcher, resource loading, execution, exactness, authority, and persistence coverage.
- `src/main/java/com/fende/obesecat/inventory/TransmutationCubeInventory.java` — atomically replace all 12 stored slots and save once.
- `src/main/java/com/fende/obesecat/inventory/TransmutationCubeMenu.java` — resolve and execute a recipe only on the logical server.
- `docs/item-obtainment-report.md` — make transmutation the sole documented acquisition route.

**Delete:**

- `src/main/resources/data/obesecat/recipe/j_robert_pacoheimer.json` — remove the old shapeless crafting-table recipe.

---

### Task 1: Build and Register the Exact Unordered Recipe Model

**Files:**

- Create: `src/main/java/com/fende/obesecat/recipe/TransmutationIngredient.java`
- Create: `src/main/java/com/fende/obesecat/recipe/TransmutationInput.java`
- Create: `src/main/java/com/fende/obesecat/recipe/TransmutationRecipe.java`
- Create: `src/main/java/com/fende/obesecat/registry/ModRecipeTypes.java`
- Modify: `src/main/java/com/fende/obesecat/ObeseCatMod.java`
- Modify: `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java`

- [ ] **Step 1: Add a failing exact-match GameTest**

Add a `transmutationRecipeMatchesExactUnorderedContents` test. Construct the recipe directly with `PACO x1` and `OPPENHEIMERS_HAT x1`, then assert:

```java
TransmutationRecipe recipe = new TransmutationRecipe(
        List.of(
                new TransmutationIngredient(ModItems.PACO.get(), 1),
                new TransmutationIngredient(ModItems.OPPENHEIMERS_HAT.get(), 1)
        ),
        new ItemStack(ModItems.J_ROBERT_PACOHEIMER.get())
);

SimpleContainer contents = new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT);
contents.setItem(11, new ItemStack(ModItems.PACO.get()));
contents.setItem(2, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
helper.assertTrue(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
        "Recipe must match items in arbitrary cube slots");

contents.clearContent();
contents.setItem(0, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
contents.setItem(10, new ItemStack(ModItems.PACO.get()));
helper.assertTrue(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
        "Recipe must ignore input ordering");

contents.setItem(5, new ItemStack(Items.DIAMOND));
helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
        "An extra item must reject an otherwise valid recipe");

contents.clearContent();
contents.setItem(0, new ItemStack(ModItems.PACO.get()));
helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
        "A missing ingredient must reject the recipe");

contents.setItem(1, new ItemStack(Items.LEATHER_HELMET));
helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
        "A wrong item must reject the recipe");

contents.setItem(2, new ItemStack(ModItems.PACO.get()));
contents.setItem(1, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
        "A duplicate ingredient must reject the exact recipe");
```

- [ ] **Step 2: Run the test suite and confirm RED**

From the feature worktree:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat compileJava
```

Expected: compilation fails because the transmutation recipe classes do not exist.

- [ ] **Step 3: Implement the counted ingredient and immutable input snapshot**

Implement `TransmutationIngredient` as a record with:

```java
public record TransmutationIngredient(Item item, int count) {
    public static final Codec<TransmutationIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec()
                    .validate(item -> item == Items.AIR
                            ? DataResult.error(() -> "Transmutation ingredients cannot be air")
                            : DataResult.success(item))
                    .fieldOf("item")
                    .forGetter(TransmutationIngredient::item),
            Codec.INT.validate(count -> count > 0
                            ? DataResult.success(count)
                            : DataResult.error(() -> "Transmutation ingredient count must be positive"))
                    .fieldOf("count")
                    .forGetter(TransmutationIngredient::count)
    ).apply(instance, TransmutationIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmutationIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.registry(Registries.ITEM), TransmutationIngredient::item,
                    ByteBufCodecs.VAR_INT, TransmutationIngredient::count,
                    TransmutationIngredient::new
            );
}
```

Implement `TransmutationInput` as an immutable deep copy. It must require exactly `TransmutationCubeInventory.SLOT_COUNT` entries and expose `copyOf(Container)`, `getItem(int)`, `size()`, and `lowestOccupiedSlot()`:

```java
public final class TransmutationInput implements RecipeInput {
    private final List<ItemStack> items;

    private TransmutationInput(List<ItemStack> items) {
        if (items.size() != TransmutationCubeInventory.SLOT_COUNT) {
            throw new IllegalArgumentException("A transmutation input must contain exactly 12 slots");
        }
        this.items = items.stream().map(ItemStack::copy).toList();
    }

    public static TransmutationInput copyOf(Container container) {
        List<ItemStack> items = new ArrayList<>(TransmutationCubeInventory.SLOT_COUNT);
        for (int slot = 0; slot < TransmutationCubeInventory.SLOT_COUNT; slot++) {
            items.add(container.getItem(slot));
        }
        return new TransmutationInput(items);
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    public int lowestOccupiedSlot() {
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()) return slot;
        }
        return -1;
    }
}
```

- [ ] **Step 4: Implement exact multiset matching and codecs**

`TransmutationRecipe.matches` must aggregate expected and actual counts by `Item` and compare the maps for equality. Do not use slot positions, stop after finding required ingredients, or tolerate extra items:

```java
private Map<Item, Integer> expectedCounts() {
    Map<Item, Integer> counts = new HashMap<>();
    ingredients.forEach(ingredient -> counts.merge(ingredient.item(), ingredient.count(), Integer::sum));
    return counts;
}

private static Map<Item, Integer> actualCounts(TransmutationInput input) {
    Map<Item, Integer> counts = new HashMap<>();
    for (int slot = 0; slot < input.size(); slot++) {
        ItemStack stack = input.getItem(slot);
        if (!stack.isEmpty()) counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
    }
    return counts;
}

@Override
public boolean matches(TransmutationInput input, Level level) {
    return expectedCounts().equals(actualCounts(input));
}
```

The recipe must return defensive result copies from `assemble` and `getResultItem`, return `ModRecipeTypes.TRANSMUTATION_SERIALIZER.get()` and `ModRecipeTypes.TRANSMUTATION.get()`, and mark itself special so it does not appear as ordinary crafting guidance.

The serializer `MapCodec` must use these fields:

```java
private static final MapCodec<TransmutationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TransmutationIngredient.CODEC.listOf()
                .validate(ingredients -> ingredients.isEmpty()
                        ? DataResult.error(() -> "A transmutation recipe needs at least one ingredient")
                        : DataResult.success(ingredients))
                .fieldOf("ingredients")
                .forGetter(TransmutationRecipe::ingredients),
        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(TransmutationRecipe::result)
).apply(instance, TransmutationRecipe::new));
```

Its `StreamCodec` must write a bounded ingredient-list size, encode each `TransmutationIngredient.STREAM_CODEC`, and encode the result with `ItemStack.STREAM_CODEC`. Reject decoded ingredient lists that are empty or exceed 12 entries.

- [ ] **Step 5: Register the recipe type and serializer**

Create `ModRecipeTypes` using two `DeferredRegister`s:

```java
public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, ObeseCatMod.MOD_ID);
public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, ObeseCatMod.MOD_ID);

public static final DeferredHolder<RecipeType<?>, RecipeType<TransmutationRecipe>> TRANSMUTATION =
        RECIPE_TYPES.register("transmutation", () -> RecipeType.simple(
                ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "transmutation")
        ));
public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TransmutationRecipe>>
        TRANSMUTATION_SERIALIZER = RECIPE_SERIALIZERS.register(
                "transmutation", TransmutationRecipe.Serializer::new
        );
```

Register both deferred registers on `modEventBus` in `ObeseCatMod` before gameplay begins.

- [ ] **Step 6: Run GREEN verification**

```powershell
.\gradlew.bat runGameTestServer
.\gradlew.bat build
```

Expected: all existing tests plus the exact unordered matcher test pass; build succeeds.

- [ ] **Step 7: Commit the recipe foundation**

```powershell
git add -- src/main/java/com/fende/obesecat/ObeseCatMod.java src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java src/main/java/com/fende/obesecat/recipe src/main/java/com/fende/obesecat/registry/ModRecipeTypes.java
git commit -m "feat: add data-driven transmutation recipes"
```

---

### Task 2: Execute the Paco Recipe Server-Side and Persist the Result

**Files:**

- Modify: `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java`
- Modify: `src/main/java/com/fende/obesecat/inventory/TransmutationCubeInventory.java`
- Modify: `src/main/java/com/fende/obesecat/inventory/TransmutationCubeMenu.java`
- Create: `src/main/resources/data/obesecat/recipe/j_robert_pacoheimer_transmutation.json`
- Delete: `src/main/resources/data/obesecat/recipe/j_robert_pacoheimer.json`

- [ ] **Step 1: Replace the inert-button test with failing execution tests**

Preserve the current quick-move, nesting, and menu-validity assertions, but rename the inert-button test to describe its remaining responsibility. Add tests that prove:

1. `obesecat:j_robert_pacoheimer_transmutation` is present in the recipe manager.
2. The old `obesecat:j_robert_pacoheimer` recipe id is absent.
3. Paco in slot 11 plus the hat in slot 2 transmutes successfully.
4. The output is J. Paco Barkkenheimer in slot 2, the lowest occupied input slot.
5. Reopening the same cube ItemStack still shows only that output.
6. Reversing the ingredient positions also succeeds.
7. Extra, missing, duplicate, and wrong items cause no mutation.
8. Button id `99` returns `false` and causes no mutation.
9. A non-`ServerPlayer` caller accepts button id `0` but does not mutate contents, preserving client-side authority boundaries.

Use `helper.makeMockServerPlayerInLevel()` for successful server execution and `helper.makeMockPlayer(GameType.CREATIVE)` for the authority/no-op case. Capture `ItemContainerContents` before every invalid click and compare it with the component afterward.

- [ ] **Step 2: Run GameTests and confirm RED**

```powershell
.\gradlew.bat runGameTestServer
```

Expected: the new tests fail because the JSON recipe is absent and the button is still inert.

- [ ] **Step 3: Add atomic inventory replacement**

Add this method to `TransmutationCubeInventory`:

```java
public void replaceContents(List<ItemStack> contents) {
    if (contents.size() != SLOT_COUNT) {
        throw new IllegalArgumentException("Cube replacement must contain exactly 12 slots");
    }
    for (int slot = 0; slot < SLOT_COUNT; slot++) {
        getItems().set(slot, contents.get(slot).copy());
    }
    setChanged();
}
```

This deliberately updates the backing list directly and calls `setChanged()` once, preventing transient half-consumed states from being written to the cube component.

- [ ] **Step 4: Execute recipes from the menu on the server only**

Replace `clickMenuButton` with this control flow:

```java
@Override
public boolean clickMenuButton(Player player, int id) {
    if (id != TRANSMUTE_BUTTON_ID) return false;
    if (!(player instanceof ServerPlayer serverPlayer)) return true;
    if (!stillValid(serverPlayer) || !(cubeInventory instanceof TransmutationCubeInventory persistentInventory)) {
        return false;
    }

    TransmutationInput input = TransmutationInput.copyOf(cubeInventory);
    Optional<RecipeHolder<TransmutationRecipe>> match = serverPlayer.level().getRecipeManager()
            .getRecipeFor(ModRecipeTypes.TRANSMUTATION.get(), input, serverPlayer.level());
    if (match.isEmpty()) return true;

    ItemStack output = match.get().value().assemble(input, serverPlayer.level().registryAccess());
    int outputSlot = input.lowestOccupiedSlot();
    if (output.isEmpty() || outputSlot < 0) return true;

    NonNullList<ItemStack> replacement = NonNullList.withSize(
            TransmutationCubeInventory.SLOT_COUNT, ItemStack.EMPTY
    );
    replacement.set(outputSlot, output);
    persistentInventory.replaceContents(replacement);
    broadcastChanges();
    return true;
}
```

Important invariants: snapshot before mutation, query only the custom recipe type, assemble before clearing inputs, replace all slots in one operation, and never mutate on an unmatched recipe.

- [ ] **Step 5: Replace the old crafting resource with the cube recipe**

Delete `src/main/resources/data/obesecat/recipe/j_robert_pacoheimer.json` and add:

```json
{
  "type": "obesecat:transmutation",
  "ingredients": [
    {
      "item": "obesecat:paco",
      "count": 1
    },
    {
      "item": "obesecat:oppenheimers_hat",
      "count": 1
    }
  ],
  "result": {
    "id": "obesecat:j_robert_pacoheimer",
    "count": 1
  }
}
```

- [ ] **Step 6: Run focused and full GREEN verification**

```powershell
.\gradlew.bat runGameTestServer
.\gradlew.bat build
```

Expected: all GameTests pass, including both arbitrary-slot orders, exact rejection cases, result placement, persistence, unknown-button rejection, and non-server no-op behavior.

- [ ] **Step 7: Commit execution and resource changes**

```powershell
git add -- src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java src/main/java/com/fende/obesecat/inventory/TransmutationCubeInventory.java src/main/java/com/fende/obesecat/inventory/TransmutationCubeMenu.java src/main/resources/data/obesecat/recipe
git commit -m "feat: transmute paco and hat in cube"
```

---

### Task 3: Document the Sole Acquisition Path and Verify the Playtest Jar

**Files:**

- Modify: `docs/item-obtainment-report.md`

- [ ] **Step 1: Update the obtainment report**

Remove J. Paco Barkkenheimer from the ordinary crafting table and reduce the documented ordinary crafting-recipe count accordingly. Add a transmutation section that states:

- Input: exactly one Paco and one Oppenheimer's Hat.
- Placement: any of the 12 cube slots, in any order.
- Restriction: no extra items or counts.
- Output: one J. Paco Barkkenheimer, left inside the cube.
- Status: this transmutation is the sole survival acquisition route; the old crafting-table route was removed.

Update the item-obtainment matrix row from `Crafting` to `Transmutation Cube`.

- [ ] **Step 2: Run static resource checks**

```powershell
rg -n 'j_robert_pacoheimer|transmutation' src/main/resources/data/obesecat/recipe docs/item-obtainment-report.md
Test-Path 'src/main/resources/data/obesecat/recipe/j_robert_pacoheimer.json'
Test-Path 'src/main/resources/data/obesecat/recipe/j_robert_pacoheimer_transmutation.json'
```

Expected: no old crafting recipe reference remains; the first `Test-Path` is `False`; the second is `True`; docs identify the cube as the sole route.

- [ ] **Step 3: Perform final clean verification**

```powershell
.\gradlew.bat runGameTestServer
.\gradlew.bat clean build
$jar = Get-ChildItem -LiteralPath 'build\libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
jar tf $jar.FullName | Select-String 'transmutation|j_robert_pacoheimer'
Get-Item -LiteralPath $jar.FullName | Select-Object FullName, Length, LastWriteTime
```

Expected: GameTests and clean build report `BUILD SUCCESSFUL`; the jar contains the custom recipe JSON and recipe classes; the output prints the exact playtest jar path and timestamp.

- [ ] **Step 4: Request code review and address only verified findings**

Use `superpowers:requesting-code-review` against the full feature diff. Pay special attention to recipe codec safety, exact-count semantics, server authority, atomic persistence, output placement, and old-recipe removal. If review raises an issue, verify it before editing and rerun the relevant tests afterward.

- [ ] **Step 5: Commit documentation**

```powershell
git add -- docs/item-obtainment-report.md
git commit -m "docs: document cube transmutation recipe"
```

---

## Manual Playtest Checklist

After automated verification, copy the newly built feature jar into the playtest instance through the project's existing mod-install workflow, then verify:

- Put Paco and Oppenheimer's Hat in distant cube slots; Transmutation produces J. Paco Barkkenheimer inside the cube.
- Reverse their positions; the same result appears.
- Add any third item; clicking Transmutation changes nothing.
- Remove either ingredient; clicking changes nothing.
- Close and reopen the cube after success; the result persists.
- A crafting table no longer offers or completes the old Paco + hat recipe.

Do not rename the jar or advance release metadata during this feature task; artifact naming remains a publish-time decision.
