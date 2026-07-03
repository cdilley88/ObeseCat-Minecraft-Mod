# Cow King Fort Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `Cow King Fort` as a naturally generating one-piece structure that appears only in the `Secret Cow Level` dimension at a slightly uncommon frequency.

**Architecture:** Reuse the mod's existing Manhattan Bunker worldgen pattern so this stays almost entirely data-driven. Copy the provided `.nbt` into the live structure template paths, register a one-entry jigsaw template pool plus structure and structure set JSON, scope the structure with a dedicated cow-level biome tag, and explicitly enable that structure set inside the flat `Secret Cow Level` dimension's `structure_overrides`.

**Tech Stack:** Minecraft 1.21.1 data-driven worldgen JSON, NeoForge 21.1.228 resource packaging, vanilla jigsaw structures, flat dimension generator settings, Java 21 Gradle build pipeline.

---

## File Map

**Create:**

- `src/main/resources/data/obesecat/structure/village/plains/houses/cow_king_fort.nbt` - live template path already documented by the mod's bunker notes.
- `src/main/resources/data/obesecat/structures/village/plains/houses/cow_king_fort.nbt` - duplicate packaged template path to mirror the repo's existing bunker convention.
- `src/main/resources/data/obesecat/worldgen/template_pool/cow_king_fort/start_pool.json` - one-piece rigid template pool.
- `src/main/resources/data/obesecat/worldgen/structure/cow_king_fort.json` - jigsaw structure definition.
- `src/main/resources/data/obesecat/worldgen/structure_set/cow_king_fort.json` - random-spread placement settings.
- `src/main/resources/data/obesecat/tags/worldgen/biome/cow_king_fort_spawnable.json` - biome tag that only includes `obesecat:secret_cow_level`.

**Modify:**

- `src/main/resources/data/obesecat/dimension/secret_cow_level.json` - replace the empty `structure_overrides` array with the cow fort structure set so the flat dimension actually generates it.

---

### Task 1: Copy The Fort Template Into Live Resource Paths

**Files:**

- Create: `src/main/resources/data/obesecat/structure/village/plains/houses/cow_king_fort.nbt`
- Create: `src/main/resources/data/obesecat/structures/village/plains/houses/cow_king_fort.nbt`

- [ ] **Step 1: Confirm the source asset exists and inspect the destination pattern**

Run:

```powershell
Get-Item -LiteralPath 'ASSETS\Custom Structures\cowkingfort.nbt' | Select-Object FullName,Length,LastWriteTime
Get-Item -LiteralPath 'src\main\resources\data\obesecat\structure\village\plains\houses\manhattan_bunker.nbt','src\main\resources\data\obesecat\structures\village\plains\houses\manhattan_bunker.nbt' | Select-Object FullName,Length
```

Expected: the new source asset is present, and both bunker destination paths exist as the template-copy pattern we are matching.

- [ ] **Step 2: Copy the new template into the singular `structure` path**

Run:

```powershell
Copy-Item -LiteralPath 'ASSETS\Custom Structures\cowkingfort.nbt' -Destination 'src\main\resources\data\obesecat\structure\village\plains\houses\cow_king_fort.nbt' -Force
Get-Item -LiteralPath 'src\main\resources\data\obesecat\structure\village\plains\houses\cow_king_fort.nbt' | Select-Object FullName,Length,LastWriteTime
```

Expected: the copied file exists at the live singular-path destination.

- [ ] **Step 3: Copy the same template into the plural `structures` path**

Run:

```powershell
Copy-Item -LiteralPath 'ASSETS\Custom Structures\cowkingfort.nbt' -Destination 'src\main\resources\data\obesecat\structures\village\plains\houses\cow_king_fort.nbt' -Force
Get-FileHash -Algorithm SHA256 'src\main\resources\data\obesecat\structure\village\plains\houses\cow_king_fort.nbt','src\main\resources\data\obesecat\structures\village\plains\houses\cow_king_fort.nbt' | Select-Object Path,Hash
```

Expected: both copied files exist and produce the same SHA-256 hash.

- [ ] **Step 4: Commit the raw template import**

```bash
git add ASSETS/Custom\ Structures/cowkingfort.nbt src/main/resources/data/obesecat/structure/village/plains/houses/cow_king_fort.nbt src/main/resources/data/obesecat/structures/village/plains/houses/cow_king_fort.nbt
git commit -m "feat: add cow king fort structure templates"
```

Expected: a small commit that only brings the `.nbt` into the live resource tree.

---

### Task 2: Register Cow King Fort As A Secret-Cow-Level Jigsaw Structure

**Files:**

- Create: `src/main/resources/data/obesecat/tags/worldgen/biome/cow_king_fort_spawnable.json`
- Create: `src/main/resources/data/obesecat/worldgen/template_pool/cow_king_fort/start_pool.json`
- Create: `src/main/resources/data/obesecat/worldgen/structure/cow_king_fort.json`
- Create: `src/main/resources/data/obesecat/worldgen/structure_set/cow_king_fort.json`
- Modify: `src/main/resources/data/obesecat/dimension/secret_cow_level.json`

- [ ] **Step 1: Write the biome tag that scopes the fort to the cow-level biome**

Create `src/main/resources/data/obesecat/tags/worldgen/biome/cow_king_fort_spawnable.json`:

```json
{
  "replace": false,
  "values": [
    "obesecat:secret_cow_level"
  ]
}
```

- [ ] **Step 2: Write the one-piece rigid template pool**

Create `src/main/resources/data/obesecat/worldgen/template_pool/cow_king_fort/start_pool.json`:

```json
{
  "elements": [
    {
      "element": {
        "element_type": "minecraft:legacy_single_pool_element",
        "location": "obesecat:village/plains/houses/cow_king_fort",
        "processors": "minecraft:empty",
        "projection": "rigid"
      },
      "weight": 1
    }
  ],
  "fallback": "minecraft:empty"
}
```

- [ ] **Step 3: Write the structure definition**

Create `src/main/resources/data/obesecat/worldgen/structure/cow_king_fort.json`:

```json
{
  "type": "minecraft:jigsaw",
  "biomes": "#obesecat:cow_king_fort_spawnable",
  "spawn_overrides": {},
  "step": "surface_structures",
  "terrain_adaptation": "none",
  "start_pool": "obesecat:cow_king_fort/start_pool",
  "size": 1,
  "start_height": {
    "absolute": 0
  },
  "use_expansion_hack": false,
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "max_distance_from_center": 128
}
```

`128` is intentionally generous for a single authored fort so the structure is not clipped by an unnecessarily tight center bound.

- [ ] **Step 4: Write the slightly-uncommon structure set**

Create `src/main/resources/data/obesecat/worldgen/structure_set/cow_king_fort.json`:

```json
{
  "structures": [
    {
      "structure": "obesecat:cow_king_fort",
      "weight": 1
    }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 34,
    "separation": 10,
    "salt": 20472615
  }
}
```

This keeps the fort less frequent than the bunker's `24/8` spread while still remaining testable in a normal exploration pass.

- [ ] **Step 5: Enable the fort structure set inside the flat dimension**

Update `src/main/resources/data/obesecat/dimension/secret_cow_level.json` so the `settings` block ends with:

```json
      "structure_overrides": [
        "obesecat:cow_king_fort"
      ]
```

The full file should remain the existing bedrock-dirt-grass superflat definition. Only replace the empty array with the new structure set entry.

- [ ] **Step 6: Run resource validation**

Run:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat processResources
.\gradlew.bat build
```

Expected: `BUILD SUCCESSFUL` with no malformed JSON, missing resource identifiers, or packaging errors.

- [ ] **Step 7: Commit the structure registration**

```bash
git add src/main/resources/data/obesecat/tags/worldgen/biome/cow_king_fort_spawnable.json src/main/resources/data/obesecat/worldgen/template_pool/cow_king_fort/start_pool.json src/main/resources/data/obesecat/worldgen/structure/cow_king_fort.json src/main/resources/data/obesecat/worldgen/structure_set/cow_king_fort.json src/main/resources/data/obesecat/dimension/secret_cow_level.json
git commit -m "feat: register cow king fort worldgen"
```

Expected: a focused worldgen commit with the tag, pool, structure, structure set, and flat-dimension override.

---

### Task 3: Verify Placement, Natural Generation, And Packaged Jar Contents

**Files:**

- Verify only; modify one of the Task 1 or Task 2 files only if a real placement issue appears.

- [ ] **Step 1: Verify the new resources are packaged into the jar**

Run:

```powershell
jar tf (Get-ChildItem -LiteralPath 'build\libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName | Select-String 'cow_king_fort|cowkingfort'
```

Expected output includes:

- `data/obesecat/structure/village/plains/houses/cow_king_fort.nbt`
- `data/obesecat/structures/village/plains/houses/cow_king_fort.nbt`
- `data/obesecat/worldgen/template_pool/cow_king_fort/start_pool.json`
- `data/obesecat/worldgen/structure/cow_king_fort.json`
- `data/obesecat/worldgen/structure_set/cow_king_fort.json`
- `data/obesecat/tags/worldgen/biome/cow_king_fort_spawnable.json`
- `data/obesecat/dimension/secret_cow_level.json`

- [ ] **Step 2: Verify direct template and structure placement**

Launch:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runClient
```

Use the existing `Cow Level Portal` to enter the `Secret Cow Level`, move to a throwaway test area, then run these commands in order:

```mcfunction
/place template obesecat:village/plains/houses/cow_king_fort
/place structure obesecat:cow_king_fort
```

Expected: both commands succeed and place the fort exactly as-authored on flat ground with no missing blocks or clipping severe enough to justify cleanup logic.

- [ ] **Step 3: Verify locate and natural generation inside Secret Cow Level**

After direct placement validation, move to fresh unexplored cow-level chunks or use a fresh test world, then run:

```mcfunction
/locate structure obesecat:cow_king_fort
```

Expected: the locate command succeeds while in the cow dimension and points at a naturally generated fort rather than reporting that no matching structure exists.

- [ ] **Step 4: Verify the fort does not leak into the Overworld**

Return to the Overworld and run:

```mcfunction
/locate structure obesecat:cow_king_fort
```

Expected: the Overworld locate command should fail to find the structure, confirming the biome tag plus flat-dimension override keep generation scoped to `Secret Cow Level`.

- [ ] **Step 5: Inspect the working tree before handoff**

Run:

```powershell
git status --short
git diff --check
```

Expected: the new cow fort files are present as intended, any unrelated pre-existing worktree changes remain untouched, and `git diff --check` returns no whitespace errors.
