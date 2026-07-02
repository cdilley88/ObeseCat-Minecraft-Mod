# Skill Sword System Design

## Goal

Add a reusable sword-skill framework that keeps the existing `Stasis Sword` cast feel as the standard for future skill swords. `Holy Sword` becomes the common transmutation base item, and each catalyst plus `Holy Sword` combination produces a separate collectible sword item with its own right-click skill.

## Scope

This design covers:

- a shared sword-shell item class for future skill swords
- refactoring `Stasis Sword` onto that shared shell as the first real implementation
- keeping `Holy Sword` as the base ingredient for future sword transmutations
- individual output items per sword skill, not one mutable sword item
- transmutation-cube recipe flow for `Holy Sword` plus catalysts

This design does not define the individual future sword abilities yet beyond the existing `Stasis Sword`.

## Desired Player Experience

- `Holy Sword` is the base sword item players collect and feed into the Transmutation Cube.
- `Holy Sword` plus a catalyst is consumed by the cube and replaced with one specific skill sword.
- Each skill sword is its own item that players can collect permanently.
- Every skill sword keeps the same basic feel:
  - unbreakable diamond-sword-style melee behavior
  - right click triggers a sword-cast swing feel
  - cooldown gates repeated casts
  - the actual special effect is unique per sword
- `Stasis Sword` remains the first live example of the system and should keep its current gameplay feel.

## Recommended Architecture

### 1. Shared sword-shell item

Add a base item class, such as `SkillSwordItem`, that owns the common sword behavior now duplicated or implied by `StasisSwordItem`:

- sword attributes and sword action support
- unbreakable-style weapon behavior
- right-click cast shell
- cooldown handling
- cast swing behavior
- optional yellow caption tooltip

The base class should not know how any specific skill works. It should only coordinate common input behavior and delegate the actual cast effect outward.

### 2. Per-sword skill implementation

Each actual sword skill remains its own concrete item and manager pairing:

- `StasisSwordItem` extends the shared base
- `StasisSwordManager` stays the owner of the stasis effect lifecycle
- future swords follow the same pattern:
  - concrete sword item class
  - concrete manager/effect class

This keeps sword items small and lets future ability logic evolve independently.

### 3. Holy Sword as base item

`Holy Sword` should also move onto the same shared sword-shell base so the system stays consistent, but it should not have a meaningful cast effect of its own. It is primarily a transmutation ingredient and a reusable base template for future skill swords.

Recommended behavior:

- left click works like the other skill swords
- right click either does nothing meaningful or returns a harmless no-op success path
- no bespoke placeholder ability should be added

This is the easiest structure to build on later because `Holy Sword` will already be living inside the same reusable system as the later derived swords.

### 4. Transmutation model

Keep the cube transmutation system simple and item-based:

- input: exactly one `Holy Sword`
- input: exactly one catalyst item
- output: exactly one distinct skill sword item

Both inputs are consumed. The result remains a normal standalone item in the cube output flow, matching the existing transmutation architecture.

This keeps collection simple and avoids data-component complexity or per-item skill-state encoding.

## Why This Approach

This is the cleanest future-proof option because it:

- preserves the already approved `Stasis Sword` feel
- avoids re-implementing the cast shell for every new sword
- keeps transmutation recipes readable and collectible-friendly
- avoids prematurely building a more abstract data-driven skill registry before there are enough swords to justify it

It also fits the repo's existing additive pattern: small item classes plus focused manager classes.

## Component Breakdown

### `SkillSwordItem` base

Responsibilities:

- expose sword actions
- expose consistent sword stats/behavior
- manage right-click cast entry
- enforce cooldown
- trigger swing/cast feel
- call into a subclass hook or delegate for the actual skill behavior

Likely extension points:

- cooldown ticks
- cast range if needed
- caption key
- server-side cast implementation

### `HolySwordItem`

Responsibilities:

- reuse the shared sword shell
- act as the common transmutation base item
- remain intentionally effect-light

### `StasisSwordItem`

Responsibilities:

- reuse the shared sword shell
- implement the specific stasis cast hook
- keep current caption and current cast timing/feel

### Skill managers

Responsibilities:

- own per-sword effect logic
- own any delayed world actions, state, hit effects, or cleanup
- remain independent from the shared sword-shell behavior

## Data and Recipe Direction

The transmutation layer should remain recipe-driven and explicit:

- one recipe per sword skill
- exact unordered matching, consistent with the current transmutation cube rules
- no extra metadata-based recipe branching

Example pattern:

- `Holy Sword` + `Stasis Catalyst` -> `Stasis Sword`
- `Holy Sword` + future catalyst -> future skill sword

## Testing Strategy

Because this changes a working sword into a reusable framework, verification should focus on behavior preservation first:

- `Stasis Sword` still right-clicks and casts exactly as before
- cooldown behavior stays intact
- sword combat behavior remains intact
- `Holy Sword` behaves like the same sword shell but without a real skill effect
- transmutation recipes consume `Holy Sword` plus catalyst and yield the expected sword item

Recommended test shape:

- extend GameTests for transmutation outputs
- add focused behavior checks for the shared sword-shell item logic where practical
- preserve or adapt `StasisSwordGameTests` instead of replacing them wholesale

## Risks

### Risk: over-abstracting too early

Mitigation:

- keep only one shared item shell
- keep each sword ability as its own manager
- do not add a global skill registry unless later growth demands it

### Risk: breaking Stasis Sword while refactoring

Mitigation:

- treat `Stasis Sword` as the gold-standard reference implementation
- preserve existing timings and cast behavior
- verify the refactor against existing stasis tests/build behavior

### Risk: Holy Sword becoming confusing if it right-clicks

Mitigation:

- keep the right-click path intentionally inert or harmless
- make its real purpose clear through recipe design and future progression docs

## Success Criteria

This design is successful if:

- `Stasis Sword` is refactored onto a reusable base without losing its current feel
- `Holy Sword` becomes the standard base ingredient for future sword-skill transmutations
- future swords can be added by creating:
  - one catalyst item
  - one concrete sword item
  - one manager/effect implementation
  - one transmutation recipe
- no data-component-based skill storage or one-item-multiple-skills complexity is required
