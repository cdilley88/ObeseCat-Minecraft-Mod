# Cow Level Portal Design

## Goal

Turn `Cow Level Portal` into a lightweight teleportation item that sends players between the Overworld and a new joke dimension called **The Secret Cow Level**.

## Player Experience

- The item remains named **Cow Level Portal**.
- It keeps `EPIC` rarity.
- Its yellow tooltip caption remains exactly: `Moo Moo Farm?`
- Right-clicking the item in the **Overworld** teleports the player into **The Secret Cow Level**.
- Right-clicking the item while inside **The Secret Cow Level** returns the player to a safe Overworld return point.
- The safe Overworld return point is:
  - the player's bed or respawn anchor location if a valid respawn position can be resolved; otherwise
  - the Overworld world spawn.
- Right-clicking the item in any other dimension does nothing.
- The item is a teleportation tool only. It does not place blocks, create a portal frame, consume itself, or require fuel.

## Secret Cow Level

The Secret Cow Level is intentionally a pointless safe joke dimension inspired by the Diablo 2 cow level, translated into Minecraft in the simplest possible way.

Its intended feel:

- flat and immediately readable;
- safe and low-pressure;
- full of cows to a weird degree; and
- not tied to progression, loot, or combat pressure.

### Terrain

- The dimension uses a vanilla-style superflat world setup.
- Terrain should be simple and stable rather than custom-noise terrain.
- No special structures, dungeons, or progression content are required.
- The level should be easy to stand in and navigate on first arrival.

### Spawning

- Only passive cows should spawn naturally in the dimension.
- Spawn density should be noticeably higher than normal passive Overworld spawning.
- The result should feel silly and obviously intentional, not subtle.
- Hostile spawns and unrelated ambient clutter should be avoided if the data path allows it cleanly.

## Architecture

### Item Logic

Create a dedicated `CowLevelPortalItem` item class rather than leaving the item as a plain `CaptionedItem`.

This class is responsible for:

- preserving the existing caption behavior;
- detecting the current dimension on right-click;
- teleporting from Overworld to the custom cow-level dimension;
- teleporting from the cow-level dimension back to a safe Overworld target; and
- doing nothing in other dimensions.

The teleport path should follow the repo's existing teleport-item pattern where possible, especially the precedents already used by `WormholeEmberItem` and `EnigmaEmberItem`.

### Dimension Definition

Define the dimension primarily through data, not bespoke Java systems.

Expected pieces:

- a dimension key for the Secret Cow Level;
- a dimension type / level stem setup compatible with the current NeoForge + Minecraft version;
- a flat world generator configuration; and
- one biome setup tuned for cow-heavy passive spawning.

The preferred implementation is the least intrusive one that produces a stable, fully enterable dimension using vanilla-style worldgen components and data files.

### Return Logic

Returning from the Secret Cow Level should not require stored portal history or per-player state.

Instead:

- resolve the player's current Overworld respawn point if possible;
- if no safe respawn point is available, fall back to Overworld shared spawn; and
- teleport there directly.

This avoids adding persistent tracking state and keeps the item stateless.

## Scope Boundaries

Included in scope:

- `Cow Level Portal` item use behavior;
- Secret Cow Level dimension registration/data;
- cow-focused safe spawning behavior; and
- minimal safe return logic to Overworld bed/spawn.

Explicitly out of scope:

- portal blocks or portal structures;
- portal animations or complex VFX;
- persistent return-to-exact-previous-position tracking;
- custom cow mobs or loot;
- dimension-exclusive resources, structures, or progression; and
- use behavior from Nether, End, or other custom dimensions beyond doing nothing.

## Failure Handling

- If the Secret Cow Level cannot be resolved server-side, the item should fail safely and do nothing.
- If the Overworld cannot be resolved for a return teleport, the item should fail safely and do nothing.
- If a respawn point exists but is unsafe or invalid, return should fall back to shared Overworld spawn.
- Teleportation must remain server-authoritative.
- Client-only activation should not duplicate or desync teleports.

## Verification

### Build Verification

- Run a full Gradle build with the workspace Java 21 toolchain.
- Confirm both Java code and worldgen/dimension data load without errors.

### Manual Playtest Verification

- Use the item in the Overworld and confirm it sends the player into the Secret Cow Level.
- Confirm the dimension terrain is flat and usable on arrival.
- Confirm the dimension contains only passive cows spawning naturally.
- Confirm cow presence feels obviously excessive compared to a normal Overworld field.
- Use the item again in the Secret Cow Level and confirm it returns the player to bed/respawn if valid.
- Break or invalidate the respawn setup and confirm fallback to Overworld spawn works.
- Use the item in Nether and End and confirm it does nothing.
- Confirm the item keeps its existing name, rarity, texture, and tooltip.

## Implementation Notes

- Prefer additive changes over broad registry or worldgen rewrites.
- Keep the custom-dimension footprint as data-driven as possible.
- Reuse existing teleport-item patterns before inventing a new teleport framework.
- If vanilla spawn controls require a small Java assist to fully suppress non-cow spawning, keep that assist tightly scoped to the Secret Cow Level only.
