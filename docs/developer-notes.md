# Developer Notes

## Transmutation System Reference

For the live Transmutation Cube architecture, recipe format, current invariants, and future-chat handoff template, see:

- [docs/transmutation-system-reference.md](./transmutation-system-reference.md)

## High-Resolution Fat Man Model Direction

Future model polish should start by checking whether GeckoLib supports this exact stack:

- Minecraft `1.21.1`
- NeoForge `21.1.228`
- Current mod id: `obesecat`
- Current entity registry id: `obesecat:obese_cat`
- Player-facing entity name: `Fat Man`

Do not change code first. Before any renderer/model migration, inspect GeckoLib compatibility for NeoForge 1.21.1 / 21.1.228. If compatible, propose a minimal migration plan from the current vanilla `CatRenderer` approach to a GeckoLib animated entity model.

Goals:

- Keep the existing mod id and entity registry stable.
- Add GeckoLib only if it is compatible with this Minecraft/NeoForge version.
- Use Blockbench-compatible model, texture, and animation assets.
- Do not rewrite unrelated systems.
- First create a minimal custom model replacement for Fat Man, then build from there.

Current renderer context:

- Fat Man currently extends vanilla `Cat`.
- Fat Man currently uses vanilla cat behavior.
- Fat Man currently renders through a custom renderer based on vanilla `CatRenderer`.
- Dynamic growth is driven by Plutonium Cat Food state on the entity.
