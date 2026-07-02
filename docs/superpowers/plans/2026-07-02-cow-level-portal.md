# Cow Level Portal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the existing `Cow Level Portal` item into a server-side teleportation item that sends players from the Overworld into a new custom `Secret Cow Level` dimension, then returns them from that dimension back to their valid bed or respawn position, falling back to the Overworld shared spawn if no valid personal respawn exists.

**Architecture:** Keep the feature additive and low-intrusion. Replace the current plain `CaptionedItem` registration with a dedicated `CowLevelPortalItem` that owns right-click behavior and caption tooltip rendering. Put the dimension lookup, target-position resolution, and cross-dimension teleport plumbing into a focused world helper so the item stays small. Define the `Secret Cow Level` almost entirely through vanilla-style JSON resources: one custom dimension type, one custom biome with only passive cow spawns at exaggerated weights, and one flat dimension generator that uses that biome everywhere. Avoid global spawn-event hooks unless verification proves the biome JSON alone is insufficient.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge 21.1.228, server-side `ServerPlayer` dimension teleport APIs, JSON dimension resources, flat world generator settings, existing ObeseCat item registration patterns, Gradle wrapper build pipeline.

---

## File Map

**Create:**

- `src/main/java/com/fende/obesecat/item/CowLevelPortalItem.java` - portal right-click behavior plus the existing yellow caption tooltip.
- `src/main/java/com/fende/obesecat/world/CowLevelPortalManager.java` - dimension keys, destination selection, safe return-target lookup, and teleport execution.
- `src/main/resources/data/obesecat/dimension_type/secret_cow_level.json` - custom dimension type for the safe flat cow dimension.
- `src/main/resources/data/obesecat/worldgen/biome/secret_cow_level.json` - biome definition with grassland ambience and cow-only passive spawn settings.
- `src/main/resources/data/obesecat/dimension/secret_cow_level.json` - flat dimension generator that uses the custom biome across the whole dimension.

**Modify:**

- `src/main/java/com/fende/obesecat/registry/ModItems.java` - swap `cow_level_portal` from `CaptionedItem` to `CowLevelPortalItem`.
- `docs/item-obtainment-report.md` - update the item matrix entry so the portal is documented as a transmutation item with teleport behavior instead of an inert placeholder.

---

### Task 1: Portal Item Shell And Teleport Manager

**Files:**

- Create: `src/main/java/com/fende/obesecat/item/CowLevelPortalItem.java`
- Create: `src/main/java/com/fende/obesecat/world/CowLevelPortalManager.java`
- Modify: `src/main/java/com/fende/obesecat/registry/ModItems.java`

- [ ] **Step 1: Create the failing item-class integration point**

Change `ModItems.COW_LEVEL_PORTAL` so it registers `CowLevelPortalItem` instead of `CaptionedItem`, and add the new import.

Run:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat compileJava
```

Expected: compilation fails because `CowLevelPortalItem` does not exist yet.

- [ ] **Step 2: Implement the item shell with the existing caption**

Create `CowLevelPortalItem.java` as a direct `Item` subclass that preserves the current tooltip text key `item.obesecat.cow_level_portal.caption`, styles it the same way as `CaptionedItem`, and overrides `use`.

Behavior in `use`:

- Execute teleport logic only on the server side.
- In the Overworld, request a teleport into `obesecat:secret_cow_level`.
- In `obesecat:secret_cow_level`, request a return teleport back to the Overworld.
- In every other dimension, return `InteractionResultHolder.pass(stack)` so the item stays inert outside the intended joke loop.
- Return `InteractionResultHolder.sidedSuccess(stack, level.isClientSide)` after a handled portal click.

- [ ] **Step 3: Build the focused teleport helper**

Create `CowLevelPortalManager.java` to own all non-UI portal logic. Keep the item class thin by moving these responsibilities here:

- Declare `ResourceKey<Level>` constants for `Level.OVERWORLD` and `obesecat:secret_cow_level`.
- Resolve the target `ServerLevel` from the current `MinecraftServer`.
- Pick the destination `BlockPos` and final `Vec3`.
- Perform the actual cross-dimension teleport for `ServerPlayer`.

Use two public entry points:

- `teleportToSecretCowLevel(ServerPlayer player)`
- `returnFromSecretCowLevel(ServerPlayer player)`

Use one private helper for the shared `player.teleportTo(...)` call path so dimension changes, yaw, pitch, and safe-height placement stay consistent.

- [ ] **Step 4: Implement deterministic destination rules**

In `CowLevelPortalManager` implement these exact target rules:

- Entering the cow level:
  - Spawn in the custom dimension at x `0`, z `0`.
  - Use the top solid or spawnable block above the flat surface and place the player one block above it.
  - Preserve the player's yaw and pitch.
- Returning from the cow level:
  - Prefer the player's personal respawn point from the Overworld if it exists and resolves to a safe standing location.
  - If there is no valid personal respawn point, use the Overworld shared spawn position.
  - Preserve yaw and pitch on return as well.

Keep the respawn lookup server-authoritative. Do not store custom portal memory in item NBT or player persistent data for this first version.

- [ ] **Step 5: Compile the new item path**

Run the same Java 21 compile command again.

Expected: `BUILD SUCCESSFUL`, with `ModItems` now compiling against the dedicated portal item and manager classes.

---

### Task 2: Data-Driven Secret Cow Level Dimension

**Files:**

- Create: `src/main/resources/data/obesecat/dimension_type/secret_cow_level.json`
- Create: `src/main/resources/data/obesecat/worldgen/biome/secret_cow_level.json`
- Create: `src/main/resources/data/obesecat/dimension/secret_cow_level.json`

- [ ] **Step 1: Add the custom dimension type**

Create `dimension_type/secret_cow_level.json` as an Overworld-like dimension type that keeps the space safe and ordinary:

- natural `true`
- ultrawarm `false`
- coordinate scale `1.0`
- beds usable
- respawn anchor disabled
- skylight enabled
- ceiling disabled
- piglin safe `false`
- raids allowed
- ambient light `0.0`
- infiniburn pointed at the vanilla Overworld tag

Use fixed min/max Y values that match standard modern Overworld bounds for a normal-feeling flat world.

- [ ] **Step 2: Add the cow-only biome**

Create `worldgen/biome/secret_cow_level.json` with:

- calm grassy visual and audio settings
- normal precipitation and temperature
- no monster spawn entries
- one passive spawn entry for `minecraft:cow`
- a noticeably exaggerated cow spawn weight and generous pack sizes so the mob density feels intentionally silly

Keep the biome itself peaceful by omission rather than by code: if an entity is not listed in the biome spawn settings, it should not be part of the biome's normal spawn table.

- [ ] **Step 3: Add the flat dimension definition**

Create `dimension/secret_cow_level.json` that points at `obesecat:secret_cow_level` as its type and uses a flat generator with:

- the custom biome `obesecat:secret_cow_level`
- vanilla-style flat layers appropriate for a simple grassy superflat world
- no structures
- no lakes
- no decoration features beyond what the flat generator and biome require by default

The resulting dimension should feel like a deliberately pointless joke arena: flat grass, empty horizon, lots of cows.

- [ ] **Step 4: Validate resource loading early**

Run:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat processResources
.\gradlew.bat compileJava
```

Expected: `BUILD SUCCESSFUL` with no malformed JSON, unknown biome references, or missing dimension identifiers.

---

### Task 3: End-To-End Portal Behavior And Documentation

**Files:**

- Modify: `src/main/java/com/fende/obesecat/item/CowLevelPortalItem.java`
- Modify: `src/main/java/com/fende/obesecat/world/CowLevelPortalManager.java`
- Modify: `docs/item-obtainment-report.md`

- [ ] **Step 1: Finish edge-case handling in the item**

Refine the `use` method so it handles all three intended cases cleanly:

- Overworld click: trigger entry teleport.
- Secret Cow Level click: trigger return teleport.
- Any other dimension: no teleport, no cooldown, no side effects.

If the target dimension is missing at runtime, fail safely by doing nothing instead of crashing the server.

- [ ] **Step 2: Make the return target safe**

In `CowLevelPortalManager`, resolve the return position conservatively:

- For a personal respawn point, validate the stored dimension is the Overworld and resolve the actual standing position.
- If vanilla respawn resolution returns no safe position, fall back immediately to the Overworld shared spawn.
- For the shared spawn fallback, place the player at the top safe position above that spawn rather than blindly at raw spawn Y.

This keeps the portal from returning players into solid blocks, void space, or broken-bed coordinates.

- [ ] **Step 3: Update player-facing documentation**

Edit the `Cow Level Portal` row in `docs/item-obtainment-report.md` so it says:

- the item is intended as the transmutation output from `Tome of Town Portal` plus `Virt's Leg`
- right-click in the Overworld sends the player to `Secret Cow Level`
- right-click inside `Secret Cow Level` returns the player to their Overworld bed or fallback world spawn
- the item remains inert in unrelated dimensions by design

Do not change the existing entries for `Virt's Leg` or `Tome of Town Portal` beyond what is needed for consistency.

---

### Task 4: Verification, Playtest, And Jar Readiness

**Files:**

- Verify only; modify a scoped file only if build or playtest reveals a defect.

- [ ] **Step 1: Run the release build**

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat clean build
```

Expected: `BUILD SUCCESSFUL` and an updated mod jar in `build\libs\`.

- [ ] **Step 2: Verify packaged portal and dimension assets**

Run:

```powershell
jar tf (Get-ChildItem -LiteralPath 'build\libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName | Select-String 'cow_level_portal|secret_cow_level|CowLevelPortal'
```

Expected output includes:

- `CowLevelPortalItem.class`
- `CowLevelPortalManager.class`
- `data/obesecat/dimension/secret_cow_level.json`
- `data/obesecat/dimension_type/secret_cow_level.json`
- `data/obesecat/worldgen/biome/secret_cow_level.json`

- [ ] **Step 3: Run the manual in-game checklist**

Launch `.\gradlew.bat runClient` and verify all of the following in one session:

1. Give yourself `obesecat:cow_level_portal` and confirm the existing epic rarity name color and tooltip `Moo Moo Farm?`.
2. Right-click in the Overworld and confirm a successful teleport into the new custom dimension.
3. Confirm the destination terrain is a plain superflat grass world.
4. Wait through several natural spawn cycles and confirm cows are the only naturally spawning mob.
5. Confirm cow density is obviously higher than a normal plains biome.
6. Right-click the portal while still in the Secret Cow Level and confirm return to a valid Overworld bed spawn.
7. Break or invalidate the bed, repeat the return trip, and confirm fallback to the Overworld shared spawn.
8. Enter another dimension such as the Nether, right-click the portal there, and confirm it does nothing.
9. Repeat the round trip in multiplayer or an integrated-host-plus-second-client test if available to confirm the behavior is server-authoritative.

- [ ] **Step 4: Capture the playtest jar**

After a successful build, identify the newest jar:

```powershell
Get-ChildItem -LiteralPath 'build\libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object Name,Length,LastWriteTime -First 3
```

Use the newest non-sources jar as the playtest artifact for the user. Do not rename the jar unless the user explicitly asks for a release/publish pass.

- [ ] **Step 5: Review the working tree**

```powershell
git status --short
git diff --check
```

Expected: only the scoped Cow Level Portal and Secret Cow Level changes are present, with no whitespace errors.
