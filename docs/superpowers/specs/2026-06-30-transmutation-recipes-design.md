# Transmutation Recipe System Design

## Goal

Activate the Transmutation Cube's button with a reusable, data-driven recipe system. The first recipe combines exactly one base Paco and one Oppenheimer's Hat into one J. Paco Barkkenheimer. This cube transmutation replaces the existing crafting-table recipe and becomes J. Paco Barkkenheimer's sole acquisition path.

## Player Experience

- Players place recipe inputs anywhere among the cube's 12 storage slots.
- Input order and slot positions do not matter.
- Clicking **Transmutation** evaluates the cube's complete contents against loaded transmutation recipes.
- A recipe succeeds only when all non-empty cube contents exactly equal one recipe's complete item/count multiset.
- For the first recipe, the cube must contain exactly one Paco and one Oppenheimer's Hat.
- On success, both inputs disappear and one J. Paco Barkkenheimer appears inside the cube.
- The output is placed in the lowest-index slot that contained an input. All other consumed input slots become empty.
- On failure, the button performs no mutation: no items are consumed, moved, created, or dropped.
- No sound, animation, status message, or additional visual effect is added in this release.

## Exact Unordered Matching

The matcher treats all 12 slots as one unordered input pool. It compares item identities and total counts, not slot locations.

A match requires all of the following:

- every recipe ingredient is present in its declared count;
- no declared ingredient has too few or too many items;
- there are no extra item types;
- there are no additional non-empty stacks beyond the exact recipe totals; and
- the recipe output can be created as a valid ItemStack.

The same recipe therefore works with Paco and the Hat in slots 0 and 1, slots 2 and 11, or any other arrangement. Swapping their positions does not change the result. Adding even one unrelated item makes the recipe fail without altering the inventory.

## Data-Driven Recipe Format

Add a custom recipe type with id `obesecat:transmutation`. Each recipe is a separate JSON resource under `data/obesecat/recipe/`, so future transmutations can be added without changing menu code.

The first recipe will be stored as `data/obesecat/recipe/j_robert_pacoheimer_transmutation.json`:

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

Ingredient entries use registered item ids and positive counts. Result entries use a registered item id and a positive count. Empty ingredient lists, air items, unknown item ids, zero or negative counts, and outputs exceeding their valid stack size are invalid recipe data.

Two recipes must not define the same exact input multiset. Duplicate signatures are invalid content authoring because they would make the intended output ambiguous.

## Architecture

### Recipe Model and Registration

Create a focused transmutation recipe model that owns:

- decoded ingredient item/count entries;
- the output ItemStack definition;
- exact unordered matching against a 12-slot input snapshot; and
- assembly of a copied output stack.

Register its custom recipe type and serializer through the mod's existing deferred-registration setup. The serializer is responsible only for JSON/network encoding and structural validation. Matching remains inside the recipe model so menus and tests do not duplicate recipe logic.

### Cube Input Snapshot

Create a small immutable recipe input representing copies of all 12 cube slots. It exposes the slot count and copied stacks required by the Minecraft recipe APIs. Recipe matching reads this snapshot and never mutates the live container.

The snapshot also records the lowest non-empty slot index for deterministic output placement after a successful match.

### Button Execution

`TransmutationCubeMenu.clickMenuButton` keeps button id `0` as the only accepted action.

On the logical server:

1. Validate that the menu still refers to the original held cube.
2. Snapshot all 12 cube slots.
3. Ask the level's recipe manager for a matching `obesecat:transmutation` recipe.
4. If no recipe matches, return successfully without changing any slot.
5. If a recipe matches, create its output before touching the live inventory.
6. Clear the exact input contents and place the output in the snapshot's lowest occupied input slot.
7. Mark the container changed so the ItemStack component and client menu synchronize immediately.

The client-side menu continues to acknowledge button id `0` without performing recipe logic. The server remains authoritative for matching, consumption, and output creation.

### Atomicity and Safety

Recipe matching and output construction complete before the live container is mutated. A malformed recipe, missing output, invalid menu, unknown button id, or non-match leaves all stacks unchanged.

Because matching requires the complete cube multiset, successful execution consumes every non-empty stack represented by the exact recipe. Future recipes with stackable inputs consume the exact declared counts; any surplus count prevents the match instead of leaving leftovers.

Cube nesting remains prohibited and is unaffected by this system.

## Existing Crafting Recipe Removal

Delete `data/obesecat/recipe/j_robert_pacoheimer.json`, which currently crafts J. Paco Barkkenheimer from Atomic Paco and Oppenheimer's Hat. No crafting-table fallback remains.

The item obtainment report must:

- remove J. Paco Barkkenheimer from the crafting recipes table;
- reduce the crafting-recipe count accordingly;
- add a Transmutation Cube recipe section or entry documenting Paco + Oppenheimer's Hat;
- identify cube transmutation as the item's sole survival acquisition path; and
- retain the distinction between cube crafting and recipes performed inside the cube.

## Verification

Automated GameTests will verify:

- Paco + Hat matches in adjacent slots;
- Paco + Hat matches in widely separated slots;
- reversing their slot order still matches;
- success consumes both inputs and places one J. Paco Barkkenheimer in the lowest occupied input slot;
- successful output persists in the cube's ItemStack component;
- an unrelated extra item prevents matching and preserves all contents;
- an extra ingredient count prevents matching and preserves all contents;
- a missing ingredient prevents matching and preserves all contents;
- a wrong item prevents matching and preserves all contents;
- unknown button ids remain rejected;
- the client-side button path does not mutate its local SimpleContainer; and
- the old crafting-table recipe is absent while the custom JSON recipe loads.

Build verification will include resource processing, a clean Gradle build, the GameTest server, and jar inspection for the custom recipe class, serializer, type, and JSON resource.

Manual in-game verification will cover clicking the real screen button, observing immediate slot synchronization, trying several slot arrangements, confirming invalid contents remain untouched, and confirming the old crafting-table combination no longer produces an output.

## Explicitly Deferred

- Additional transmutation recipes beyond Paco + Oppenheimer's Hat.
- Tags, alternatives, wildcards, component-sensitive ingredients, durability rules, or remainder items.
- Sounds, animation, particles, button-state previews, recipe hints, or failure messages.
- Client-side recipe prediction.
- Batch transmutation or repeated execution from one click.
