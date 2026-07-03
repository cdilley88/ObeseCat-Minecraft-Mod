# Cow King Fort Design

## Goal

Add `Cow King Fort` as a naturally generating structure in the `Secret Cow Level` dimension. It should generate as a standalone, slightly uncommon structure using the existing data-driven structure pattern already proven by `Manhattan Bunker`.

## Scope

This design covers:

- copying the new `cowkingfort.nbt` template into the live structure data path
- creating the data files needed for a one-piece jigsaw structure
- scoping the structure so it only generates in `Secret Cow Level`
- choosing slightly uncommon generation settings
- verifying the structure can be placed, located, and naturally generated

This design does not cover:

- the `Cow King` mob itself
- loot, chests, villagers, or special inhabitants
- custom structure processors unless testing proves they are necessary
- changes to the Secret Cow Level terrain shape beyond what already exists

## Desired Player Experience

- Players exploring `Secret Cow Level` should occasionally find a `Cow King Fort`.
- The fort should feel like a rare-ish point of interest rather than something constant.
- The structure should appear exactly as authored from the `.nbt` template unless testing shows a placement issue.
- The fort should exist now as a clean worldgen anchor for the later `Cow King` mob work.

## Recommended Approach

### 1. Reuse the Manhattan Bunker structure pattern

The cleanest, least intrusive approach is to reuse the same broad structure stack already working for `Manhattan Bunker`:

- structure template `.nbt`
- single-piece template pool
- jigsaw structure JSON
- structure set JSON

This keeps the implementation data-driven and aligned with proven repo patterns instead of inventing a one-off Java structure placer.

### 2. Keep the fort as a single rigid piece

`Cow King Fort` should be treated as one authored structure, not a modular village-style assembly. That means:

- one start pool
- one rigid element
- no follow-on pieces
- no dynamic connectors beyond the minimum jigsaw compatibility needed for structure registration

This is the simplest way to get stable generation from the provided `.nbt`.

### 3. Scope generation only to Secret Cow Level

The structure should not be available in the Overworld or elsewhere. It should only participate in worldgen inside `Secret Cow Level`.

Preferred implementation:

- define a biome tag used only by the Secret Cow Level biome, then point the structure at that tag

Fallback if the current 1.21.1 worldgen wiring makes that awkward:

- directly reference the custom biome in the structure definition if that is the cleaner supported format

### 4. No cleanup logic unless testing proves it is needed

Because `Secret Cow Level` is already a superflat grass world with no terrain noise or other structures, the default assumption should be:

- place the fort exactly as authored
- avoid extra ground padding, terrain carving, or block replacement logic

If the template sinks, floats, or clips in testing, we can add a tiny cleanup pass later. For the first pass, that would be unnecessary complexity.

## Why This Approach

This is the best fit because it:

- matches existing repo worldgen structure patterns
- keeps the feature almost entirely data-driven
- avoids unnecessary Java logic
- gives the later `Cow King` mob work a stable destination structure
- keeps the change small and additive inside an already working custom dimension

## Component Breakdown

### Structure template asset

Source:

- `ASSETS/Custom Structures/cowkingfort.nbt`

Live packaged target:

- copy into the same kind of structure data path already used by `Manhattan Bunker`

This keeps template loading consistent with the mod's existing structure workflow.

### Template pool

Responsibilities:

- define a one-entry start pool
- point at the `Cow King Fort` template
- use rigid projection
- keep the structure single-piece

### Structure JSON

Responsibilities:

- define the fort as a jigsaw structure
- point at the start pool
- use the same broad placement style as the bunker unless Secret Cow Level testing suggests a simpler variant
- scope it to the cow-level biome only

### Structure set JSON

Responsibilities:

- define spacing, separation, and salt
- make the fort slightly uncommon

Recommended starting feel:

- rarer than a village
- common enough that a player intentionally exploring the cow dimension can find one without absurd travel

## Generation Tuning

The exact spacing values do not need to imitate villages perfectly. The design target is:

- slightly uncommon
- not clustered constantly
- still practical to test in a play session

Recommended starting range:

- spacing around `30-40`
- separation around `8-12`

That is intentionally looser than the bunker's current test-heavy frequency.

If the first playtest feels too common or too rare, spacing can be tuned without redesigning the system.

## Testing Strategy

Verification should happen in this order:

### 1. Data-load correctness

- resource/data pack loads cleanly
- no malformed structure or pool JSON
- no missing template references

### 2. Direct command validation

- `/place structure obesecat:cow_king_fort`
- `/locate structure obesecat:cow_king_fort`

These should be the first proof that registration and placement work before relying on natural generation luck.

### 3. In-dimension natural generation check

- enter `Secret Cow Level`
- explore or regenerate enough chunks to confirm natural spawning occurs
- confirm the structure appears only there

### 4. Placement sanity check

- verify the fort sits acceptably on the flat grass surface
- confirm no obvious clipping, floating, or terrain corruption

If there is a placement issue, that is when we consider a tiny cleanup adjustment.

## Risks

### Risk: the `.nbt` lacks the minimal jigsaw compatibility needed for this structure path

Mitigation:

- first verify command placement and registration
- if needed, make only the smallest template compatibility adjustment required

### Risk: structure scoping leaks into other dimensions or biomes

Mitigation:

- scope structure generation to the Secret Cow Level biome/tag only
- validate with command checks and by inspecting structure JSON carefully

### Risk: generation is technically correct but too rare to notice

Mitigation:

- start with intentionally testable "slightly uncommon" spacing
- tune spacing later without changing the overall design

### Risk: the fort sits awkwardly despite the flat dimension

Mitigation:

- start without cleanup logic
- add the smallest possible placement correction only if playtesting proves it is needed

## Success Criteria

This design is successful if:

- `Cow King Fort` is registered as a standalone structure
- it can be placed and located by command
- it naturally generates in `Secret Cow Level`
- it does not naturally generate outside that dimension
- it appears as-authored from the provided `.nbt`
- no extra mob, loot, or progression logic is bundled into this first pass
