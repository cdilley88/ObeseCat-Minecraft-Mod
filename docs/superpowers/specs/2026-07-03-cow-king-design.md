# Cow King Design

## Goal

Add a dedicated `Cow King` entity that spawns inside each `Cow King Fort` as a giant joke-boss placeholder. For this first pass, the Cow King should look huge and memorable, but it should not lock us into the final combat tuning yet.

## Scope

This design covers:

- a custom `CowKing` entity class based on the cow base
- a client renderer that scales the cow model to `5.0x`
- a fort-scoped spawner that places one Cow King per fort
- entity registration and client renderer registration
- a localized display name of `The Cow King`

This design does not cover:

- hostile AI, attack damage, health, movement speed, or boss phases
- boss bar, loot, drops, or special death behavior
- custom sounds or textures unless they become necessary later
- spawn eggs or natural spawn rules
- changes to Cow King Fort worldgen itself

## Desired Player Experience

- Entering a Cow King Fort should feel like finding a ridiculous boss room.
- The Cow King should be visually absurd and immediately readable as a joke boss.
- Exactly one Cow King should appear per fort.
- Reloading the same chunks should not create duplicate Cow Kings.
- The first pass should look special without forcing us to commit to the final fight design yet.

## Recommended Approach

### 1. Dedicated custom entity

Create a `CowKing` class that extends the cow base so it can reuse cow movement, sounds, and general body plan.

Recommended behavior for this pass:

- keep the entity visually cow-like
- reuse the vanilla cow texture and model
- do not add a new texture asset yet
- keep the model scale change in the renderer, not in the entity hitbox

This keeps the entity easy to upgrade later when you want real boss behavior.

### 2. Fort-scoped spawner

Mirror the existing bunker resident pattern with a dedicated fort spawner:

- listen for chunk loads on the server
- inspect structure starts for `cow_king_fort`
- if the fort is present and no tagged Cow King exists in the structure box, spawn one
- mark the entity so repeated chunk loads do not duplicate it

This keeps the spawn logic tied to the fort instead of world-global events.

### 3. Visual-only first pass

Keep the first version intentionally light:

- no attack AI
- no damage tuning
- no 300-health boss stats yet
- no 3/4 speed tuning yet
- no special loot or combat reward pass yet

That lets us test the joke-boss silhouette first and add the real fight later without reworking the spawn plumbing.

## Why This Approach

This is the cleanest path because it:

- reuses the repo's existing chunk-load resident pattern
- keeps the feature fully custom, which helps if more bosses are added later
- avoids premature boss-combat tuning when the current goal is a visual placeholder
- keeps the render change separate from the entity and the spawn hook
- gives us a stable place to layer in hostile behavior later

## Component Breakdown

### `CowKing` entity

Responsibilities:

- exist as the dedicated mob type for the fort boss placeholder
- keep cow-like movement and sounds
- carry the localized name `The Cow King`
- rely on the fort spawner instead of natural spawning
- remain simple enough to extend later into a real boss

### `CowKingRenderer`

Responsibilities:

- reuse the vanilla cow renderer/model
- scale the render pose to `5.0x`
- keep the visual identity simple and readable

The renderer should handle the giant look; the entity itself should stay normal-sized for this pass so collision and pathing stay predictable.

### `CowKingFortSpawner`

Responsibilities:

- detect `Cow King Fort` structure starts on chunk load
- spawn one Cow King per fort if none is already present
- tag the entity to prevent duplicate spawns
- place the entity at a sensible point inside the fort bounding box

The spawner should be the only place this mob enters the world for now.

### Registration points

Responsibilities:

- `ModEntities` registers the `CowKing` entity type
- `ObeseCatMod` wires the server-side spawn hook
- `ObeseCatModClient` wires the renderer
- `en_us.json` adds the display name `The Cow King`

## Data Flow

1. A Cow King Fort chunk loads on the server.
2. The spawner inspects the chunk's structure starts.
3. If the fort is present and no existing Cow King is tagged in that fort box, the spawner creates one.
4. The entity is named `The Cow King` and marked persistent.
5. The client renderer draws it at 5x scale using the cow model.
6. Later boss tuning can add hostile AI and combat stats without changing the spawn flow.

## Testing Strategy

Verification should happen in this order:

### 1. Build and resource validation

- run `processResources`
- run `build`
- confirm no entity registration, lang, or client wiring errors

### 2. Spawn verification

- enter a Cow King Fort
- confirm exactly one Cow King appears
- reload the same area or revisit the same chunks
- confirm the fort does not duplicate the boss

### 3. Visual verification

- confirm the entity name renders as `The Cow King`
- confirm the model reads as roughly 5x the normal cow size
- confirm the giant scale does not produce obvious rendering glitches

### 4. Regression check

- confirm the fort still spawns correctly
- confirm the Cow King addition does not break the existing cow dimension or portal flow

## Risks

### Risk: the visual scale and the hitbox feel mismatched

Mitigation:

- keep the hitbox normal for this pass
- if the mismatch feels bad, adjust the hitbox in a later tuning pass after the visual placeholder is proven

### Risk: duplicate spawns on repeated chunk loads

Mitigation:

- use a dedicated structure tag on the entity
- check for existing Cow Kings inside the structure box before spawning
- mark the entity persistent

### Risk: adding combat too early makes the boss harder to tune later

Mitigation:

- keep the current pass visual-only
- defer hostile AI, damage, health, and speed to a later follow-up

### Risk: custom entity work expands into unnecessary boss features

Mitigation:

- keep the first pass focused on just three things: spawn, name, and scale
- do not add a boss bar or loot until the real fight pass exists

## Success Criteria

This design is successful if:

- each `Cow King Fort` spawns exactly one Cow King
- the mob renders as a giant cow and is named `The Cow King`
- repeated chunk loads do not create duplicates
- the first pass remains visually funny without committing to final combat tuning
- later boss work can layer onto the same custom entity cleanly
