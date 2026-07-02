# ObeseCat Item Obtainment Report

Generated: 2026-06-19

This report covers how current ObeseCat items, blocks, and registered content variants become obtainable in survival-like play. It inspected:

- `src/main/java/com/fende/obesecat/registry/ModItems.java`
- `src/main/java/com/fende/obesecat/registry/ModVillagerTrades.java`
- `src/main/java/com/fende/obesecat/registry/ModVillagers.java`
- `src/main/java/com/fende/obesecat/registry/ModLootTables.java`
- `src/main/java/com/fende/obesecat/world/ManhattanBunkerDebug.java`
- `src/main/java/com/fende/obesecat/world/ManhattanBunkerResidentSpawner.java`
- `src/main/java/com/fende/obesecat/world/ManhattanPhysicistSpawner.java`
- `src/main/resources/data/obesecat/recipe/*.json`
- `src/main/resources/data/obesecat/worldgen/**/*.json`
- `src/main/resources/data/obesecat/neoforge/biome_modifier/*.json`
- `src/main/resources/data/obesecat/painting_variant/*.json`
- `src/main/resources/data/minecraft/tags/painting_variant/*.json`
- `src/main/java/com/fende/obesecat/ObeseCatMod.java`

Creative tab registration is listed separately because it exposes items in creative mode but does not make them survival-obtainable.

## Acquisition Overview

Current acquisition mechanisms:

| Mechanism | Current location | Notes |
| --- | --- | --- |
| Farmer trades | `ModVillagerTrades.java` | Adds five novice Farmer trades. |
| Manhattan Physicist trades | `ModVillagerTrades.java` and `ModVillagers.java` | Adds Trinitite-priced trades to Manhattan Physicists spawned in bunkers. |
| Manhattan bunker standalone worldgen | `data/obesecat/worldgen/structure/manhattan_bunker.json`, `data/obesecat/worldgen/structure_set/manhattan_bunker.json`, `data/obesecat/worldgen/template_pool/manhattan_bunker/start_pool.json`, and `data/obesecat/structure/village/plains/houses/manhattan_bunker.nbt` | Uses the proper jigsaw structure system, independent of village generation. |
| Loot injection | `ModLootTables.java` | Adds runtime loot pools to vanilla dungeon, End City, and Bastion loot tables. |
| Crafting recipes | `src/main/resources/data/obesecat/recipe/*.json` | Adds seven crafting recipes. |
| Transmutation recipes | `src/main/resources/data/obesecat/recipe/*_transmutation.json` | Adds one cube-based transmutation route. |
| Fat Man detonation | `ObeseCat.java` | Adds one Trinitite block at the crater center for lithium-deuteride Fat Man detonations. |
| Painting variant registry | `data/obesecat/painting_variant/*.json` and `data/minecraft/tags/painting_variant/placeable.json` | Adds custom artwork to the vanilla Painting item variant pool. |
| Creative tabs | `ObeseCatMod.java` | Adds many items to creative tabs only. Not survival acquisition. |

## Villager Trades

Trades are currently added to Farmer villagers and Manhattan Physicist villagers at novice level.

Farmer shared trade settings:

| Field | Value |
| --- | --- |
| Profession | Farmer |
| Level | Novice, level `1` |
| Cost | `1` emerald |
| Sale count | `1` item |
| Max uses | `12` |
| Villager XP | `1` |
| Price multiplier | `0.05` |

Farmer items sold:

| Item | Cost | Output | Max uses | XP | Notes |
| --- | ---: | ---: | ---: | ---: | --- |
| Fat Man Spawn Egg | 1 emerald | 1 | 12 | 1 | Base access to Fat Man entity. |
| Plutonium Cat Food | 1 emerald | 1 | 12 | 1 | Also used in Atomic Paco crafting. |
| Paco | 1 emerald | 1 | 12 | 1 | Base of the Paco crafting chain. |
| Ember | 1 emerald | 1 | 12 | 1 | Base of Ember Singularity crafting. |
| Mr. Kitty | 1 emerald | 1 | 12 | 1 | Base of Tiny Planet crafting. |

Balance note: these are all novice Farmer trades at a flat 1 emerald. That makes several item chains mostly limited by vanilla rare components, not by the mod's own economy.

Manhattan Physicist shared trade settings:

| Field | Value |
| --- | --- |
| Profession | Manhattan Physicist |
| Level | Novice, level `1` |
| Currency | Trinitite |
| Max uses | `12` |
| Villager XP | `1` |
| Price multiplier | `0.05` |

Manhattan Physicist items sold:

| Item | Cost | Output | Max uses | XP | Notes |
| --- | ---: | ---: | ---: | ---: | --- |
| Hellhound Paco | 1 Trinitite | 1 | 12 | 1 | First test trade for the Trinitite economy. Replaces the old Hellhound Paco crafting recipe. |
| Oppenheimer's Hat | 5 Trinitite | 1 | 12 | 1 | Rare trade-only transmutation component for J. Paco Barkkenheimer. |

## Worldgen Structures

| Structure-like feature | Spawn hook | Spawn rate | Biomes | Generation step | Contents |
| --- | --- | ---: | --- | --- | --- |
| Manhattan Bunker | Standalone jigsaw structure | Test spacing `24`, separation `8`, random spread, weight `1` | Plains, sunflower plains, snowy plains, desert, savanna, savanna plateau, badlands, eroded badlands, wooded badlands, and meadow | Surface structures | Exported bunker `.nbt`, plus one Manhattan Physicist spawned by code at bunker load time near the Nuclear Library if no resident already exists. |

Generation constraints:

| Field | Value |
| --- | --- |
| Footprint | 16x8-ish bunker template, compact enough for standalone surface generation |
| Biome tag | `#obesecat:manhattan_bunker_spawnable`: plains, sunflower plains, snowy plains, desert, savanna, savanna plateau, badlands, eroded badlands, wooded badlands, and meadow |
| Placement behavior | Uses built-in jigsaw structure placement with `WORLD_SURFACE_WG` projection from a fixed bunker template |
| Main blocks | Whatever is saved in `manhattan_bunker.nbt`: smooth stone, smooth stone slabs, polished andesite, iron bars, iron doors, hanging lanterns, and the saved interior. Any vanilla lectern in the placed template is converted into the custom Nuclear Library workstation block during placement/load. |
| Developer marker | Removed. All naturally generated bunkers now use the same plain bunker template with no beacon variant |
| Locator support | Registered as `obesecat:manhattan_bunker`, so `/locate structure obesecat:manhattan_bunker` and `/place structure obesecat:manhattan_bunker` should be the first structure-level checks once the resource pack loads cleanly |

Standalone structure notes:

| Field | Value |
| --- | --- |
| Template source | `ASSETS/Custom Structures/manhattanbunker.nbt`, copied into `data/obesecat/structure/village/plains/houses/manhattan_bunker.nbt` for live template loading. A duplicate is also kept in `data/obesecat/structures/...` so the packaged asset mirrors the generated-structure folder naming. |
| Start pool | `obesecat:manhattan_bunker/start_pool`, one rigid `legacy_single_pool_element`, `minecraft:empty` processors |
| Structure id | `obesecat:manhattan_bunker` |
| Structure set | `obesecat:manhattan_bunker`, random spread placement, spacing `24`, separation `8`, salt `19631946` |
| Jigsaw depth | Structure `size` is `1`, not `0`, because this version does not emit a valid placed structure for a single-piece jigsaw structure at depth zero |
| Spawn behavior | Standalone overworld structure in the biome tag, intentionally high-frequency for this test pass |
| Connector | One added `minecraft:jigsaw` connector at the front of the template, replaced with air after placement |
| Physicist spawn | `ManhattanBunkerResidentSpawner` scans each bunker start chunk, converts any vanilla lectern to the custom Nuclear Library workstation, then spawns exactly one tagged villager near that workstation if none already exists inside the bunker bounds. `ManhattanPhysicistSpawner` then normalizes that villager into the Manhattan Physicist profession on join |
| Debug command | `/obesecat place_bunker [pos]` | Places the bunker template directly from the loaded resource manager so the NBT can be tested without village generation. |
| Vanilla test commands | `/place template obesecat:village/plains/houses/manhattan_bunker`, `/place structure obesecat:manhattan_bunker`, `/locate structure obesecat:manhattan_bunker` |

## Loot Tables

Loot is injected at runtime by `ModLootTables.addLoot`. Each entry creates a separate loot pool with exactly `1` roll, guarded by a random chance condition. The listed chance is the chance per generated loot table/chest instance.

| Vanilla loot table | Item | Pool name | Chance | Rolls | Count |
| --- | --- | --- | ---: | ---: | ---: |
| `minecraft:chests/simple_dungeon` | Toilet | `obesecat:dungeon_toilet` | 22% | 1 | 1 |
| `minecraft:chests/simple_dungeon` | Mr. Kitty's Paws | `obesecat:dungeon_mr_kittys_paws` | 8% | 1 | 1 |
| `minecraft:chests/end_city_treasure` | Lithium Deuteride Cat Food | `obesecat:end_city_lithium_deuteride` | 28% | 1 | 1 |
| `minecraft:chests/end_city_treasure` | Wormhole Ember | `obesecat:end_city_wormhole_ember` | 18% | 1 | 1 |
| `minecraft:chests/end_city_treasure` | Enigma Ember | `obesecat:end_city_enigma_ember` | 18% | 1 | 1 |
| `minecraft:chests/bastion_treasure` | Night Vision Mr. Kitty | `obesecat:bastion_night_vision_mr_kitty` | 12% | 1 | 1 |
| `minecraft:chests/bastion_other` | Night Vision Mr. Kitty | `obesecat:bastion_night_vision_mr_kitty` | 12% | 1 | 1 |
| `minecraft:chests/bastion_bridge` | Night Vision Mr. Kitty | `obesecat:bastion_night_vision_mr_kitty` | 12% | 1 | 1 |
| `minecraft:chests/bastion_hoglin_stable` | Night Vision Mr. Kitty | `obesecat:bastion_night_vision_mr_kitty` | 12% | 1 | 1 |
| `minecraft:entities/warden` | Virt's Leg | `obesecat:warden_virts_leg` | 100% | 1 | 1 |

Combined chance notes, assuming independent pools:

| Loot table | Combined outcome | Chance |
| --- | --- | ---: |
| Simple Dungeon | At least one ObeseCat loot item | 28.24% |
| Simple Dungeon | Both Toilet and Mr. Kitty's Paws | 1.76% |
| End City Treasure | At least one ObeseCat loot item | 51.59% |
| End City Treasure | All three End City ObeseCat items | 0.91% |
| Any single Bastion table listed above | Night Vision Mr. Kitty | 12% |

## Crafting Recipes

| Output | Recipe type | Ingredients | Minimum notable cost if base mod items are bought |
| --- | --- | --- | --- |
| Concussive Paco | Shapeless | Paco + Piston | 1 emerald + 1 piston |
| Attack Paco | Shapeless | Concussive Paco + Diamond | 1 emerald + 1 piston + 1 diamond |
| Assault Paco | Shapeless | Attack Paco + Nether Star | 1 emerald + 1 piston + 1 diamond + 1 nether star |
| Sniper Paco | Shapeless | Assault Paco + Nether Star + Spyglass | 1 emerald + 1 piston + 1 diamond + 2 nether stars + 1 spyglass |
| Atomic Paco | Shapeless | Hellhound Paco + Plutonium Cat Food | 1 Trinitite + 1 emerald |
| Tiny Planet | Shapeless | Mr. Kitty + Nether Star | 1 emerald + 1 nether star |
| Ember Singularity | Shaped | 8 Eyes of Ender around Ember | 1 emerald + 8 eyes of ender |

## Transmutation Recipes

| Output | Recipe type | Inputs | Notes |
| --- | --- | --- | --- |
| J. Paco Barkkenheimer | Transmutation Cube | Exactly one Paco and one Oppenheimer's Hat, in any order, in any of the cube's 12 slots; no extra items or counts | One J. Paco Barkkenheimer is left inside the cube. This transmutation is the sole survival acquisition route; the old crafting-table route was removed. |

## Weapon Balance Notes

| Item | Range | Damage | Reload/Cooldown | Notes |
| --- | ---: | ---: | --- | --- |
| Attack Paco | 20 blocks | 1 heart | Existing stink meter cooldown | Shortened from the old shared Paco targeting range. |
| Assault Paco | 20 blocks | 4 hearts per damaging bark | Existing stink meter cooldown | Inherits Attack Paco's 20 block range and keeps the three-bark burst sound effect. |
| Sniper Paco | 50 blocks | 8 hearts | After firing, next right-click starts a 2.5 second reload delay | Long-range alternative to Assault Paco. A miss still spends the loaded shot. |

## Fat Man Detonation

| Trigger | Output | Quantity | Placement | Notes |
| --- | --- | ---: | --- | --- |
| Fat Man reaches detonation after being fed Lithium Deuteride Cat Food | Trinitite | 1 block | Center floor of the crater | Does not apply to Plutonium Cat Food detonations or handheld nuclear items. |

## Painting Variants

Painting variants use the vanilla Painting item, not a new mod item. Survival cost is the vanilla Painting recipe cost: 8 sticks + 1 wool for 1 Painting item.

Vanilla placement picks from placeable variants that fit the wall, then chooses randomly among the largest surviving variants. If the wall is constrained to a 2x4 valid painting space, the current ObeseCat 2x4 paintings are Domino, Dorito, Ember, and Paco, so each has a 25% chance before any future 2x4 variants are added. On larger valid walls, larger paintings can win the selection pass before these 2x4 variants are considered.

| Variant | Item display | Title | Artist | Size | Texture | Survival obtainment |
| --- | --- | --- | --- | --- | --- | --- |
| `obesecat:domino` | Painting | Domino | Casey Dilley | 2x4 | `assets/obesecat/textures/painting/domino.png` | Place a vanilla Painting on a wall where a 2x4 painting is the largest valid option. Current constrained 2x4 chance: 25%. Also appears as a preset Painting in creative/search through the placeable painting variant tag. |
| `obesecat:dorito` | Painting | Dorito | Casey Dilley | 2x4 | `assets/obesecat/textures/painting/dorito.png` | Place a vanilla Painting on a wall where a 2x4 painting is the largest valid option. Current constrained 2x4 chance: 25%. Also appears as a preset Painting in creative/search through the placeable painting variant tag. |
| `obesecat:ember` | Painting | Ember | Casey Dilley | 2x4 | `assets/obesecat/textures/painting/ember.png` | Place a vanilla Painting on a wall where a 2x4 painting is the largest valid option. Current constrained 2x4 chance: 25%. Also appears as a preset Painting in creative/search through the placeable painting variant tag. |
| `obesecat:paco` | Painting | Paco | Casey Dilley | 2x4 | `assets/obesecat/textures/painting/paco.png` | Place a vanilla Painting on a wall where a 2x4 painting is the largest valid option. Current constrained 2x4 chance: 25%. Also appears as a preset Painting in creative/search through the placeable painting variant tag. |

## Item-By-Item Matrix

| Item | Survival obtainment | Details |
| --- | --- | --- |
| Fat Man Spawn Egg | Farmer trade | Novice Farmer, 1 emerald, 12 max uses. |
| Manhattan Physicist Spawn Egg | Creative-only test item | Added to the Spawn Eggs creative tab for direct villager testing. |
| Plutonium Cat Food | Farmer trade | Novice Farmer, 1 emerald, 12 max uses. |
| Lithium Deuteride Cat Food | Loot | End City Treasure, 28%. |
| Paco | Farmer trade | Novice Farmer, 1 emerald, 12 max uses. |
| Ember | Farmer trade | Novice Farmer, 1 emerald, 12 max uses. |
| Mr. Kitty | Farmer trade | Novice Farmer, 1 emerald, 12 max uses. |
| Virt's Leg | Loot | Warden, 100%. No crafting recipe yet. Rare rarity. Caption: Really? A Cow Level? |
| Tome of Town Portal | No survival path found yet | Registered as a rare item with caption `Yep, a Cow Level`. Intended as the second half of a future transmutation recipe, but currently has no crafting recipe, loot source, or use behavior. |
| Cow Level Portal | Planned transmutation output | Registered as an epic item with caption `Moo Moo Farm?`. Intended as the transmutation output from Tome of Town Portal plus Virt's Leg. Right-click in the Overworld sends the player to Secret Cow Level; right-click inside Secret Cow Level returns the player to their Overworld bed or fallback world spawn. It remains inert in unrelated dimensions by design. |
| Night Vision Mr. Kitty | Loot | Bastion treasure, other, bridge, and hoglin stable tables, 12% each. |
| Mr. Kitty's Paws | Loot | Simple Dungeon, 8%. |
| Ember Singularity | Crafting | Ember + 8 Eyes of Ender. |
| Wormhole Ember | Loot | End City Treasure, 18%. |
| Enigma Ember | Loot | End City Treasure, 18%. |
| Tiny Planet | Crafting | Mr. Kitty + Nether Star. |
| Transmutation Cube | Crafting | `obesecat:transmutation_cube` is survival-obtainable from a shapeless Chest + Crafting Table + Trinitite block recipe. Its persistent 12-slot inventory works, and it now powers the cube's transmutation recipes. |
| Concussive Paco | Crafting | Paco + Piston. |
| Attack Paco | Crafting | Concussive Paco + Diamond. |
| Assault Paco | Crafting | Attack Paco + Nether Star. |
| Sniper Paco | Crafting | Assault Paco + Nether Star + Spyglass. Fires one 50-block, 8-heart shot, then requires a right-click reload with a 2.5 second delay before firing again. Caption: Long Range Barks. Rare rarity. |
| J. Paco Barkkenheimer | Transmutation Cube | Exactly one Paco and one Oppenheimer's Hat, in any order, in any of the cube's 12 slots; no extra items or counts. Output stays inside the cube. | This transmutation is the sole survival acquisition route; the old crafting-table route was removed. |
| Atomic Paco | Crafting | Hellhound Paco + Plutonium Cat Food. Hellhound Paco now comes from Manhattan Physicist trading, making the notable minimum cost 1 Trinitite + 1 emerald. |
| Hellhound Paco | Manhattan Physicist trade | Spawned Manhattan Physicist in a Manhattan Bunker, 1 Trinitite, 12 max uses, 1 XP, 0.05 price multiplier. |
| Oppenheimer's Hat | Manhattan Physicist trade | Spawned Manhattan Physicist in a Manhattan Bunker, 5 Trinitite, 12 max uses, 1 XP, 0.05 price multiplier. |
| Boom Stick | No survival path found | Registered and added to creative tab, but no trade, loot, or recipe found. |
| Fire Stick | No survival path found | Registered and added to creative tab, but no trade, loot, or recipe found. |
| Fire Boom Stick | No survival path found | Registered and added to creative tab, but no trade, loot, or recipe found. |
| BIG Fire Boom Stick | No survival path found | Registered and added to creative tab, but no trade, loot, or recipe found. |
| Toilet | Loot | Simple Dungeon, 22%. |
| Trinitite | Fat Man detonation | One block at the crater center when Fat Man detonates from Lithium Deuteride Cat Food. |
| Nuclear Library | Structure workstation / creative block | Custom glowing bookshelf-styled workstation block used only for the Manhattan Physicist POI. It replaces any vanilla lectern placed in the Manhattan Bunker, has uncommon rarity as an item, and emits lime-green / neon-yellow particles for quick visual identification. |
| Manhattan Bunker | Standalone worldgen structure | Proper jigsaw structure registration in the tagged flat-ish overworld biomes, using only the plain bunker template. |
| Manhattan Physicist | Bunker spawn | Spawned by bunker-resident code near the bunker Nuclear Library workstation, then normalized to the custom Manhattan Physicist profession. Current trades: Hellhound Paco for 1 Trinitite and Oppenheimer's Hat for 5 Trinitite. |
| Domino painting variant | Vanilla Painting item | Cost is 8 sticks + 1 wool for the Painting item. Variant metadata: Domino, Casey Dilley, 2x4. |
| Dorito painting variant | Vanilla Painting item | Cost is 8 sticks + 1 wool for the Painting item. Variant metadata: Dorito, Casey Dilley, 2x4. |
| Ember painting variant | Vanilla Painting item | Cost is 8 sticks + 1 wool for the Painting item. Variant metadata: Ember, Casey Dilley, 2x4. |
| Paco painting variant | Vanilla Painting item | Cost is 8 sticks + 1 wool for the Painting item. Variant metadata: Paco, Casey Dilley, 2x4. |

## Current Gaps And Risks

- There is no single source of truth for item acquisition. Trades live in Java, loot injection lives in Java, recipes live in JSON, and creative tab exposure lives elsewhere.
- Manhattan Bunker spawning now depends on the data-driven structure system loading cleanly. If no bunkers appear, the first checks should be `/place structure obesecat:manhattan_bunker`, `/locate structure obesecat:manhattan_bunker`, and the startup logs from `ManhattanBunkerDebug`.
- Manhattan Physicist POI registration now depends on the custom `obesecat:nuclear_library` block states, not `minecraft:lectern`. Reusing vanilla lectern states here will crash mod loading because a single blockstate can only belong to one POI type.
- `/place template obesecat:village/plains/houses/manhattan_bunker` and `/obesecat place_bunker` are the fastest ways to prove the template itself loads.
- If grass still appears inside the bunker, re-save the structure block template after clearing the interior volume with air included; the saved NBT currently includes air blocks, but a re-save is the cleanest way to eliminate any terrain bleed from the source template.
- Painting variants are data-driven, but they are not yet included in a centralized mod content catalog.
- Several registered items have no survival acquisition route. If they are debug or creative-only items, they should be explicitly marked that way somewhere.
- Manhattan Physicist Spawn Egg is intentionally creative-only for testing the bunker villager path.
- Farmer trades are very cheap and very early. A novice Farmer can sell the spawn egg and multiple base items for 1 emerald each.
- End City loot has a high combined chance: about 51.59% for at least one ObeseCat item in an End City treasure chest.
- Loot chances are not currently data-driven or grouped by item tier/stage. Balancing requires manual code inspection.
- There are no recipe unlock advancements or player-facing guidance files in the current data folder.
- Curios tags exist for some items, but those tags only define equip slots. They do not create acquisition paths.

## Recommended Standardized System

Create a small obtainment catalog and make every item declare one of these statuses:

- `trade`
- `loot`
- `recipe`
- `derived_recipe`
- `worldgen_feature`
- `special_event`
- `creative_only`
- `disabled`
- `planned`

Suggested catalog fields:

| Field | Purpose |
| --- | --- |
| `item` | Registry id, such as `obesecat:enigma_ember`. |
| `displayName` | Human-readable report name. |
| `tier` | Rough progression tier, such as early, mid, late, endgame, debug. |
| `intendedStage` | When players should reasonably obtain it. |
| `acquisition` | Array of trade, loot, recipe, or creative-only entries. |
| `chance` | Loot probability as a decimal and percent. |
| `cost` | Emerald cost, recipe ingredients, or other cost. |
| `maxUses` | Trade max uses where relevant. |
| `xp` | Trade XP where relevant. |
| `priceMultiplier` | Villager price multiplier where relevant. |
| `sourceTables` | Loot table ids where relevant. |
| `dependencies` | Required prior mod items or vanilla milestones. |
| `balanceNotes` | Short note explaining why the cost/chance is set this way. |
| `playtestStatus` | Untested, needs tuning, accepted, too cheap, too rare, etc. |

Practical implementation path:

1. Add a repo-owned catalog, likely `docs/obtainment-catalog.json` first.
2. Add a validation task that checks every `ModItems` entry has a catalog record.
3. Add a report generator that creates this Markdown report from the catalog plus live recipe JSON.
4. Convert `ModVillagerTrades` to read from a Java-side catalog/record list so trades are not hard-coded one by one.
5. Convert `ModLootTables` to read from the same catalog/record list so percentages are visible and testable.
6. Add a lightweight playtest log format for changes, expected stage, actual player feedback, and follow-up tuning.

The important rule: no item should exist in `ModItems` without an explicit acquisition status. Even if that status is `creative_only`, the system should know it on purpose.
