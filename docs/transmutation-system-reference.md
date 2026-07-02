# Transmutation System Reference

This document is the source of truth for ObeseCat's Transmutation Cube system.

Use it when:

- adding new transmutation recipes;
- checking whether a requested recipe can be data-only;
- verifying how the cube currently behaves; or
- giving future chats a quick technical briefing on the system.

## What the system is

The Transmutation Cube is a held item with a persistent internal inventory and a dedicated GUI.

Current player-facing behavior:

- right click opens the cube UI;
- the cube stores items persistently inside the item itself;
- the storage grid is 3 columns by 4 rows, for 12 total slots;
- clicking the **Transmutation** button checks the full cube contents against loaded transmutation recipes; and
- on success, the inputs are consumed and the result remains inside the cube.

## Core rules and invariants

These rules define the current system and should be preserved unless we intentionally redesign it:

- The cube always exposes exactly 12 storage slots.
- Recipes are exact and unordered.
- Inputs may be placed in any of the 12 slots.
- Slot position does not matter.
- Matching is based on the exact item/count multiset across all non-empty cube slots.
- Any extra item causes the recipe to fail.
- Any missing item causes the recipe to fail.
- Any wrong item causes the recipe to fail.
- Any duplicate item beyond the declared exact counts causes the recipe to fail.
- A successful transmutation consumes the matched inputs and places the result in the lowest occupied input slot.
- The current implementation leaves only the result in the cube after a successful transmutation.
- The cube cannot be placed inside another cube.
- The transmute button is server-authoritative: the client may acknowledge the button, but only the server mutates inventory.
- A successful transmutation currently plays the `transmute` sound effect.

## Mental model for future recipe additions

Yes: this is already a proven repeatable system.

In the normal case, adding a new recipe does not require new menu logic, GUI work, or new matching code. A new recipe is usually just a new JSON file under the transmutation recipe data folder, assuming the needed input and output items already exist.

That means future requests can usually be handled in this format:

> Add a transmutation recipe with these exact inputs, in any cube slots, producing this result.

## Recipe format

Transmutation recipes use the custom recipe type:

`obesecat:transmutation`

Each recipe lives in:

`src/main/resources/data/obesecat/recipe/`

### Copyable template

```json
{
  "type": "obesecat:transmutation",
  "ingredients": [
    {
      "item": "namespace:first_input",
      "count": 1
    },
    {
      "item": "namespace:second_input",
      "count": 1
    }
  ],
  "result": {
    "id": "namespace:result_item",
    "count": 1
  }
}
```

### Format rules

- `ingredients` must contain at least one entry.
- A recipe cannot declare more than 12 ingredient entries.
- Each ingredient uses an item id plus a positive count.
- The result uses an item id plus a positive count.
- The output must assemble into a valid `ItemStack`.
- Recipes should be authored with one ingredient entry per item type whenever possible.
- Do not create two recipes with the same exact input multiset.

## Existing live recipes

These are the currently confirmed transmutation recipe JSONs:

- `hellhound_paco_transmutation.json`
  - Paco + Blaze Powder -> Hellhound Paco
- `j_robert_pacoheimer_transmutation.json`
  - Paco + Oppenheimer's Hat -> J. Paco Barkkenheimer

## Code touchpoints

These are the main files future work should inspect before changing system behavior:

### Item, inventory, and UI

- `src/main/java/com/fende/obesecat/item/TransmutationCubeItem.java`
- `src/main/java/com/fende/obesecat/inventory/TransmutationCubeInventory.java`
- `src/main/java/com/fende/obesecat/inventory/TransmutationCubeMenu.java`
- `src/main/java/com/fende/obesecat/inventory/TransmutationCubeSlot.java`
- `src/main/java/com/fende/obesecat/client/TransmutationCubeScreen.java`
- `src/main/java/com/fende/obesecat/registry/ModMenus.java`

### Recipe system

- `src/main/java/com/fende/obesecat/recipe/TransmutationRecipe.java`
- `src/main/java/com/fende/obesecat/recipe/TransmutationInput.java`
- `src/main/java/com/fende/obesecat/registry/ModRecipeTypes.java`

### Registration and assets

- `src/main/java/com/fende/obesecat/registry/ModItems.java`
- `src/main/resources/data/obesecat/recipe/transmutation_cube.json`
- `src/main/resources/assets/obesecat/lang/en_us.json`
- `src/main/resources/assets/obesecat/models/item/transmutation_cube.json`

### Verification

- `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java`

## What the tests already prove

The current GameTests already cover the important system guarantees:

- exact unordered matching;
- failure when extra items are present;
- failure when ingredients are missing;
- failure when the wrong item is present;
- failure when duplicate ingredients exceed the exact recipe;
- persistent cube storage;
- cube anti-nesting behavior;
- transmutation result persistence after reopening;
- result placement in the lowest occupied input slot; and
- presence of the current transmutation recipe data.

## Recommended workflow for adding a new recipe

1. Confirm the input items and output item already exist.
2. Add one new transmutation JSON file under `data/obesecat/recipe/`.
3. Keep the recipe exact and orderless.
4. If the recipe changes item acquisition, update player-facing docs as needed.
5. Add or extend a GameTest when the new recipe introduces a new rule or edge case.
6. Build a playtest jar when the user wants an in-game verification pass.

## Future-chat handoff template

If a future thread needs to continue transmutation work, this short brief should be enough:

> Read `docs/transmutation-system-reference.md` first. The Transmutation Cube is a 12-slot persistent held-item inventory with exact unordered recipe matching across any slots. Recipes are usually data-only JSON files under `src/main/resources/data/obesecat/recipe/` using type `obesecat:transmutation`. Matching fails on extra, missing, wrong, or duplicate items beyond the declared exact counts. Successful transmutation leaves the result in the lowest occupied input slot and currently plays the transmute sound.

