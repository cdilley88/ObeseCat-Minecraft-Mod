# Stasis Sword Center Snap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Center the Stasis Sword ice formation on the targeted block and snap nearby entities into the encasement before the ice closes.

**Architecture:** Keep the change concentrated in the existing Stasis Sword placement manager so the item use path stays thin. The placement code will anchor the structure on the targeted block instead of offsetting it, then it will move living entities within a small radius to the center point before the ice blocks are placed.

**Tech Stack:** Java 21, NeoForge 1.21.1, Minecraft structure templates, existing gametest coverage.

---

### Task 1: Center the formation and snap nearby living entities

**Files:**
- Modify: `src/main/java/com/fende/obesecat/world/StasisSwordManager.java`

- [ ] **Step 1: Adjust the placement anchor and entity handling**

```java
public static Optional<FrozenFormation> place(ServerLevel level, BlockPos origin) {
    Optional<StructureTemplate> template = level.getStructureManager().get(STASIS_TEMPLATE_ID);
    if (template.isEmpty()) {
        return Optional.empty();
    }

    StructureTemplate loadedTemplate = template.get();
    List<StructureTemplate.StructureBlockInfo> blocks = loadedTemplate.filterBlocks(origin, new StructurePlaceSettings(), Blocks.ICE, true);
    if (blocks.isEmpty()) {
        return Optional.empty();
    }

    Vec3 center = Vec3.atCenterOf(origin);
    AABB captureBox = new AABB(center.x - 1.0D, center.y - 0.5D, center.z - 1.0D, center.x + 1.0D, center.y + 1.5D, center.z + 1.0D);
    for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, captureBox, entity -> entity.isAlive())) {
        entity.teleportTo(center.x, center.y, center.z);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hasImpulse = true;
    }

    List<BlockPos> placedBlocks = new ArrayList<>();
    for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
        BlockPos pos = blockInfo.pos();
        BlockState existingState = level.getBlockState(pos);
        if (!canOverwriteWithIce(existingState)) {
            continue;
        }

        if (level.setBlock(pos, blockInfo.state(), 3)) {
            placedBlocks.add(pos.immutable());
        }
    }

    if (placedBlocks.isEmpty()) {
        return Optional.empty();
    }

    FrozenFormation formation = new FrozenFormation(origin.immutable(), List.copyOf(placedBlocks));
    PENDING_FORMATIONS.add(new PendingFormation(level, formation, SHATTER_DELAY_TICKS));
    return Optional.of(formation);
}
```

- [ ] **Step 2: Keep the existing soft-block replacement rules and shatter behavior intact**

```java
private static boolean canOverwriteWithIce(BlockState state) {
    return state.isAir()
            || state.canBeReplaced()
            || state.getBlock() instanceof BushBlock
            || state.getBlock() instanceof DoublePlantBlock
            || state.is(Blocks.SHORT_GRASS)
            || state.is(Blocks.TALL_GRASS)
            || state.is(Blocks.FERN)
            || state.is(Blocks.LARGE_FERN);
}
```

### Task 2: Update tests for centered placement and entity snap

**Files:**
- Modify: `src/main/java/com/fende/obesecat/gametest/StasisSwordGameTests.java`

- [ ] **Step 1: Add assertions that the target block remains the center anchor**

```java
helper.assertTrue(
        helper.getLevel().getBlockState(origin).isAir(),
        "The Stasis Sword should not replace the targeted block itself"
);
```

- [ ] **Step 2: Keep the delayed shatter expectation and ensure the test still passes with the centered formation**

```java
helper.runAfterDelay(StasisSwordManager.SHATTER_DELAY_TICKS + 1L, () -> {
    helper.assertTrue(
            helper.getLevel().getBlockState(firstBlock).isAir(),
            "The stasis ice structure should shatter after 40 ticks"
    );
    helper.succeed();
});
```

- [ ] **Step 3: Run the focused test and full build**

Run:

```powershell
$env:JAVA_HOME = (Resolve-Path 'G:\ObeseCat Minecraft Mod\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& '.\gradlew.bat' test --tests 'com.fende.obesecat.item.StasisSwordItemTest'
& '.\gradlew.bat' build
```

Expected: both commands succeed, and the jar is refreshed in `build/libs`.
