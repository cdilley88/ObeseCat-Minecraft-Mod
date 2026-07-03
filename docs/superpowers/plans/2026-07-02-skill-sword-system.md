# Skill Sword System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a reusable sword-skill item shell, refactor `Stasis Sword` onto it without changing its gameplay feel, turn `Holy Sword` into the base shell item, and add the first cube transmutation path `Holy Sword + Holy Knight Token -> Stasis Sword`.

**Architecture:** Add one focused `SkillSwordItem` base class that owns the shared sword behavior: diamond-sword attributes, unbreakable-style tool behavior, right-click cast entry, cooldown gating, cast swing feel, and optional yellow caption text. Keep each real ability in its own manager, so `StasisSwordManager` remains the owner of the stasis effect while `HolySwordItem` stays intentionally inert and `StasisSwordItem` only supplies its specific cast hook and caption. The first catalyst stays data-driven through the existing transmutation system, using one new item plus one recipe JSON.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge 21.1.228, NeoForge GameTest, vanilla item components, existing `obesecat:transmutation` recipe system, JSON item assets/resources.

---

## File Map

**Create:**

- `src/main/java/com/fende/obesecat/item/SkillSwordItem.java` - shared sword shell with common right-click cast flow, cooldown handling, swing behavior, sword actions, foil option, and optional caption hook.
- `src/main/java/com/fende/obesecat/item/HolySwordItem.java` - inert base sword that extends the shared shell and acts as the reusable transmutation ingredient.
- `src/main/java/com/fende/obesecat/gametest/SkillSwordGameTests.java` - focused behavior checks for the shared sword shell and Holy Sword no-op path.
- `src/main/resources/assets/obesecat/models/item/holy_knight_token.json` - generated item model for the first catalyst.
- `src/main/resources/data/obesecat/recipe/stasis_sword_transmutation.json` - exact unordered cube recipe for `Holy Sword + Holy Knight Token -> Stasis Sword`.

**Modify:**

- `src/main/java/com/fende/obesecat/item/StasisSwordItem.java` - refactor to extend `SkillSwordItem` and delegate only the cast-specific behavior.
- `src/main/java/com/fende/obesecat/registry/ModItems.java` - register `HolySwordItem`, the new `Holy Knight Token` catalyst, and a shared sword-properties factory.
- `src/main/java/com/fende/obesecat/gametest/StasisSwordGameTests.java` - preserve current stasis guarantees while asserting the refactored item still exposes the intended sword-shell feel.
- `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java` - verify the new stasis transmutation recipe is loaded and assembles correctly.
- `src/main/resources/assets/obesecat/lang/en_us.json` - add the catalyst display name and caption key for Holy Sword if needed.
- `src/main/resources/assets/obesecat/models/item/holy_sword.json` - only if the refactor changes the item parent or generated-layer format.
- `docs/item-obtainment-report.md` - document Holy Sword, Holy Knight Token, and the first sword transmutation route.

**Move / Copy Asset Inputs:**

- `ASSETS/HolyKnightToken.png` -> `src/main/resources/assets/obesecat/textures/item/holy_knight_token.png`

---

### Task 1: Build the Shared Sword Shell with Failing Tests First

**Files:**

- Create: `src/main/java/com/fende/obesecat/item/SkillSwordItem.java`
- Create: `src/main/java/com/fende/obesecat/item/HolySwordItem.java`
- Create: `src/main/java/com/fende/obesecat/gametest/SkillSwordGameTests.java`
- Modify: `src/main/java/com/fende/obesecat/registry/ModItems.java`

- [ ] **Step 1: Add failing GameTests for the reusable sword shell**

Create `SkillSwordGameTests` with one Holy Sword no-op test and one generic shell-behavior test:

```java
@GameTest(template = "village/plains/houses/manhattan_bunker")
public static void holySwordUsesSharedSwordShellWithoutCasting(GameTestHelper helper) {
    ItemStack sword = new ItemStack(ModItems.HOLY_SWORD.get());
    helper.assertTrue(
            sword.getItem().getUseAnimation(sword) == UseAnim.NONE,
            "Holy Sword should inherit the no-held-use sword cast feel"
    );
    helper.assertTrue(
            sword.getItem().canPerformAction(sword, ItemAbilities.SWORD_SWEEP),
            "Holy Sword should expose default sword actions through the shared shell"
    );
    helper.assertTrue(
            sword.getMaxStackSize() == 1,
            "Holy Sword should stay a single-stack weapon item"
    );
    helper.succeed();
}

@GameTest(template = "village/plains/houses/manhattan_bunker")
public static void holySwordRightClickPassesWithoutCooldown(GameTestHelper helper) {
    var player = helper.makeMockServerPlayerInLevel();
    ItemStack sword = new ItemStack(ModItems.HOLY_SWORD.get());
    player.setItemInHand(InteractionHand.MAIN_HAND, sword);

    InteractionResultHolder<ItemStack> result =
            sword.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertTrue(result.getResult() == InteractionResult.PASS,
            "Holy Sword should intentionally pass on right click while it has no skill");
    helper.assertTrue(result.getObject().is(ModItems.HOLY_SWORD.get()),
            "Holy Sword should keep the held stack on right click");
    helper.assertFalse(player.getCooldowns().isOnCooldown(ModItems.HOLY_SWORD.get()),
            "Holy Sword should not enter cooldown when it has no real skill");
    helper.succeed();
}
```

- [ ] **Step 2: Run the tests and confirm RED**

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat compileJava
```

Expected: compilation fails because `SkillSwordItem`, `HolySwordItem`, and `SkillSwordGameTests` do not exist yet.

- [ ] **Step 3: Implement the shared `SkillSwordItem` base class**

Create the shared shell with explicit extension points for cooldown, caption key, foil, and server-side cast success:

```java
public abstract class SkillSwordItem extends Item {
    protected SkillSwordItem(Properties properties) {
        super(properties);
    }

    protected int cooldownTicks() {
        return 0;
    }

    protected boolean isFoilByDefault() {
        return false;
    }

    protected @Nullable String captionKey() {
        return null;
    }

    protected boolean cast(ServerLevel level, Player player, InteractionHand usedHand, ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        boolean castWorked = level instanceof ServerLevel serverLevel
                && cast(serverLevel, player, usedHand, stack);

        if (!castWorked) {
            return InteractionResultHolder.pass(stack);
        }

        player.swing(usedHand, true);
        int cooldown = cooldownTicks();
        if (cooldown > 0) {
            player.getCooldowns().addCooldown(this, cooldown);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        return 0;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isFoilByDefault();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String key = captionKey();
        if (key != null) {
            tooltipComponents.add(Component.translatable(key).withStyle(ChatFormatting.YELLOW));
        }
    }
}
```

- [ ] **Step 4: Implement `HolySwordItem` and shared sword registration helpers**

Create `HolySwordItem` as an inert shell:

```java
public final class HolySwordItem extends SkillSwordItem {
    public HolySwordItem(Properties properties) {
        super(properties);
    }
}
```

In `ModItems`, add one shared factory so future swords reuse the same registration settings:

```java
private static Item.Properties skillSwordProperties(Rarity rarity) {
    return new Item.Properties()
            .stacksTo(1)
            .rarity(rarity)
            .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F))
            .component(DataComponents.TOOL, SwordItem.createToolProperties());
}
```

Then change the Holy Sword registration to:

```java
public static final DeferredItem<HolySwordItem> HOLY_SWORD = ITEMS.registerItem(
        "holy_sword",
        HolySwordItem::new,
        skillSwordProperties(Rarity.UNCOMMON)
);
```

- [ ] **Step 5: Run GREEN verification for the new shared shell**

```powershell
.\gradlew.bat runGameTestServer --tests "obesecat:holySword*"
.\gradlew.bat compileJava
```

Expected: the new GameTests pass, and the shared shell compiles cleanly without touching Stasis Sword yet.

- [ ] **Step 6: Commit the sword shell foundation**

```powershell
git add -- src/main/java/com/fende/obesecat/item/SkillSwordItem.java src/main/java/com/fende/obesecat/item/HolySwordItem.java src/main/java/com/fende/obesecat/gametest/SkillSwordGameTests.java src/main/java/com/fende/obesecat/registry/ModItems.java
git commit -m "feat: add reusable skill sword shell"
```

---

### Task 2: Refactor Stasis Sword onto the Shared Shell Without Changing Its Feel

**Files:**

- Modify: `src/main/java/com/fende/obesecat/item/StasisSwordItem.java`
- Modify: `src/main/java/com/fende/obesecat/registry/ModItems.java`
- Modify: `src/main/java/com/fende/obesecat/gametest/StasisSwordGameTests.java`

- [ ] **Step 1: Add a failing behavior-preservation test for the refactored item shell**

Extend `StasisSwordGameTests` with one focused item-level test before editing the implementation:

```java
@GameTest(template = TEMPLATE)
public static void stasisSwordStillUsesSharedSwordShell(GameTestHelper helper) {
    ItemStack sword = new ItemStack(ModItems.STASIS_SWORD.get());
    helper.assertTrue(
            sword.getItem().getUseAnimation(sword) == UseAnim.NONE,
            "Stasis Sword should keep the instant sword-cast use animation"
    );
    helper.assertTrue(
            sword.getItem().canPerformAction(sword, ItemAbilities.SWORD_SWEEP),
            "Stasis Sword should still expose default sword actions after the refactor"
    );
    helper.assertTrue(
            sword.getItem().isFoil(sword),
            "Stasis Sword should stay visually enchanted"
    );
    helper.succeed();
}
```

- [ ] **Step 2: Run the focused tests and confirm RED if shell hooks are missing**

```powershell
.\gradlew.bat runGameTestServer --tests "obesecat:stasisSwordStillUsesSharedSwordShell"
```

Expected: either compilation fails if the new test references missing imports/hooks, or the test fails until `StasisSwordItem` moves onto the shared shell.

- [ ] **Step 3: Refactor `StasisSwordItem` to extend `SkillSwordItem`**

Replace the one-off behavior with only its stasis-specific cast hook:

```java
public final class StasisSwordItem extends SkillSwordItem {
    public StasisSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int cooldownTicks() {
        return StasisSwordManager.COOLDOWN_TICKS;
    }

    @Override
    protected boolean isFoilByDefault() {
        return true;
    }

    @Override
    protected String captionKey() {
        return "item.obesecat.stasis_sword.caption";
    }

    @Override
    protected boolean cast(ServerLevel level, Player player, InteractionHand usedHand, ItemStack stack) {
        HitResult hitResult = player.pick(StasisSwordManager.RANGE, 1.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
            return false;
        }

        StasisSwordManager.schedule(level, blockHit.getBlockPos());
        return true;
    }
}
```

Update the `STASIS_SWORD` registration to reuse the shared properties helper:

```java
public static final DeferredItem<StasisSwordItem> STASIS_SWORD = ITEMS.registerItem(
        "stasis_sword",
        StasisSwordItem::new,
        skillSwordProperties(Rarity.RARE)
);
```

- [ ] **Step 4: Preserve and tighten stasis behavior verification**

Keep the three existing world-behavior tests intact and add one explicit cooldown assertion around the cast path:

```java
var player = helper.makeMockServerPlayerInLevel();
ItemStack sword = new ItemStack(ModItems.STASIS_SWORD.get());
player.setItemInHand(InteractionHand.MAIN_HAND, sword);
player.teleportTo(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() - 3.5D);
player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(origin));

InteractionResultHolder<ItemStack> result = sword.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
helper.assertTrue(result.getObject().is(ModItems.STASIS_SWORD.get()),
        "Stasis Sword should keep the held stack when cast");
helper.assertTrue(player.getCooldowns().isOnCooldown(ModItems.STASIS_SWORD.get()),
        "A successful stasis cast should still apply cooldown");
```

- [ ] **Step 5: Run focused and full GREEN verification**

```powershell
.\gradlew.bat runGameTestServer --tests "obesecat:stasisSword*"
.\gradlew.bat runGameTestServer --tests "obesecat:holySword*"
.\gradlew.bat compileJava
```

Expected: the new item-level stasis test passes, the existing stasis structure tests still pass unchanged, and Holy Sword still stays inert.

- [ ] **Step 6: Commit the stasis refactor**

```powershell
git add -- src/main/java/com/fende/obesecat/item/StasisSwordItem.java src/main/java/com/fende/obesecat/registry/ModItems.java src/main/java/com/fende/obesecat/gametest/StasisSwordGameTests.java
git commit -m "refactor: move stasis sword onto skill sword shell"
```

---

### Task 3: Add the First Catalyst and Stasis Transmutation Route

**Files:**

- Modify: `src/main/java/com/fende/obesecat/registry/ModItems.java`
- Modify: `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java`
- Modify: `src/main/resources/assets/obesecat/lang/en_us.json`
- Create: `src/main/resources/assets/obesecat/models/item/holy_knight_token.json`
- Create: `src/main/resources/data/obesecat/recipe/stasis_sword_transmutation.json`
- Modify: `docs/item-obtainment-report.md`

- [ ] **Step 1: Add a failing transmutation GameTest for the first sword recipe**

Extend `TransmutationCubeGameTests` with a resource-loading check matching the current cube-test style:

```java
@GameTest(template = TEMPLATE)
public static void stasisSwordTransmutationRecipeIsLoaded(GameTestHelper helper) {
    ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
            ObeseCatMod.MOD_ID,
            "stasis_sword_transmutation"
    );
    helper.assertTrue(
            helper.getLevel().getRecipeManager().byKey(recipeId).isPresent(),
            "The Stasis Sword transmutation recipe must be loaded"
    );

    TransmutationRecipe recipe = helper.getLevel().getRecipeManager()
            .byKey(recipeId)
            .map(recipeHolder -> (TransmutationRecipe) recipeHolder.value())
            .orElseThrow();

    SimpleContainer contents = new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT);
    contents.setItem(3, new ItemStack(ModItems.HOLY_SWORD.get()));
    contents.setItem(7, new ItemStack(ModItems.HOLY_KNIGHT_TOKEN.get()));
    helper.assertTrue(
            recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
            "Holy Sword plus Holy Knight Token must match the Stasis Sword recipe in any cube slots"
    );
    helper.assertTrue(
            recipe.assemble(TransmutationInput.copyOf(contents), helper.getLevel().registryAccess())
                    .is(ModItems.STASIS_SWORD.get()),
            "The first skill-sword recipe must output Stasis Sword"
    );
    helper.succeed();
}
```

- [ ] **Step 2: Run the GameTests and confirm RED**

```powershell
.\gradlew.bat runGameTestServer --tests "obesecat:stasisSwordTransmutationRecipeIsLoaded"
```

Expected: the test fails because the catalyst item and recipe JSON do not exist yet.

- [ ] **Step 3: Register the first catalyst item and wire its assets**

In `ModItems`, add:

```java
public static final DeferredItem<CaptionedItem> HOLY_KNIGHT_TOKEN = ITEMS.registerItem(
        "holy_knight_token",
        properties -> new CaptionedItem(properties, "item.obesecat.holy_knight_token.caption"),
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
);
```

Add the matching language keys:

```json
"item.obesecat.holy_knight_token": "Holy Knight Token",
"item.obesecat.holy_knight_token.caption": "A blade remembers its oath."
```

Create the model:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "obesecat:item/holy_knight_token"
  }
}
```

Copy the texture asset:

```powershell
Copy-Item -LiteralPath 'ASSETS\HolyKnightToken.png' -Destination 'src\main\resources\assets\obesecat\textures\item\holy_knight_token.png' -Force
```

- [ ] **Step 4: Add the first sword transmutation recipe**

Create `src/main/resources/data/obesecat/recipe/stasis_sword_transmutation.json`:

```json
{
  "type": "obesecat:transmutation",
  "ingredients": [
    {
      "item": "obesecat:holy_sword",
      "count": 1
    },
    {
      "item": "obesecat:holy_knight_token",
      "count": 1
    }
  ],
  "result": {
    "id": "obesecat:stasis_sword",
    "count": 1
  }
}
```

- [ ] **Step 5: Update obtainment docs for the new sword pipeline**

Add or revise these entries in `docs/item-obtainment-report.md`:

- `Holy Sword` -> base Transmutation Cube sword ingredient; uncommon rarity; intentionally inert right click.
- `Holy Knight Token` -> first sword catalyst item; currently only used for the Stasis Sword transmutation.
- `Stasis Sword` -> Transmutation Cube route: exactly one Holy Sword and one Holy Knight Token, any two cube slots, no extra items.

Also update the transmutation summary table so it contains the new first skill-sword recipe alongside the existing Paco and Cow Level entries.

- [ ] **Step 6: Run GREEN verification for recipes, docs, and build output**

```powershell
.\gradlew.bat runGameTestServer --tests "obesecat:stasisSwordTransmutationRecipeIsLoaded"
.\gradlew.bat runGameTestServer --tests "obesecat:*Transmutation*"
.\gradlew.bat build
rg -n "holy_knight_token|stasis_sword_transmutation|holy_sword" src/main/resources/assets/obesecat/lang/en_us.json src/main/resources/data/obesecat/recipe docs/item-obtainment-report.md
```

Expected: the new stasis transmutation test passes, the existing cube tests still pass, the build succeeds, and the grep confirms the item name, caption, and recipe references are all present.

- [ ] **Step 7: Commit the first sword recipe content**

```powershell
git add -- src/main/java/com/fende/obesecat/registry/ModItems.java src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java src/main/resources/assets/obesecat/lang/en_us.json src/main/resources/assets/obesecat/models/item/holy_knight_token.json src/main/resources/assets/obesecat/textures/item/holy_knight_token.png src/main/resources/data/obesecat/recipe/stasis_sword_transmutation.json docs/item-obtainment-report.md
git commit -m "feat: add first skill sword transmutation"
```

---

### Task 4: Final Verification and Playtest Jar

**Files:**

- Verify only; no planned source edits unless a failing test exposes a real regression.

- [ ] **Step 1: Run the full automated verification pass**

```powershell
.\gradlew.bat runGameTestServer
.\gradlew.bat clean build
```

Expected: all sword, transmutation, and existing gameplay tests pass with `BUILD SUCCESSFUL`.

- [ ] **Step 2: Inspect the final jar contents**

```powershell
$jar = Get-ChildItem -LiteralPath 'build\libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
jar tf $jar.FullName | Select-String 'SkillSwordItem|holy_knight_token|stasis_sword_transmutation|holy_sword'
Get-Item -LiteralPath $jar.FullName | Select-Object FullName, Length, LastWriteTime
```

Expected: the jar contains the new shared sword class, catalyst assets, and transmutation recipe, and the output prints the exact playtest artifact path.

- [ ] **Step 3: Request code review before closing**

Use `superpowers:requesting-code-review` on the full diff. Ask the reviewer to focus on shared item-shell regressions, cooldown semantics, right-click pass-vs-success behavior, transmutation exactness, and whether Holy Sword staying inert is clear enough for future expansion.

- [ ] **Step 4: Perform the in-game manual playtest sweep**

Verify these specific cases in a playtest world:

- Holy Sword left-clicks like a normal diamond sword.
- Holy Sword right-click does nothing visible and does not consume durability.
- Stasis Sword still schedules the ice formation at the targeted block and still shatters on time.
- `Holy Sword + Holy Knight Token` in arbitrary cube slots becomes `Stasis Sword`.
- Adding any third item causes the transmutation to fail.
- Reopening the cube after a successful transmutation preserves the Stasis Sword output.

- [ ] **Step 5: Commit any review-driven fixes, then record the playtest jar path**

```powershell
git status --short
$jar = Get-ChildItem -LiteralPath 'build\libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$jar.FullName
```

Expected: the worktree is clean after any final fix commit, and the jar path is ready for playtest handoff.
