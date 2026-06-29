# Transmutation Cube Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a rare, non-stackable Transmutation Cube that opens a persistent 3-by-4 portable inventory with blocked cube nesting and an intentionally inert Transmutation button.

**Architecture:** Store each cube's 12 items in the vanilla `DataComponents.CONTAINER` component through a focused `TransmutationCubeInventory`. Open a server-authoritative `TransmutationCubeMenu` from the hand used to right-click, synchronize it through a registered NeoForge menu type, and render it with a lightweight client screen that uses vanilla slot sprites. Keep the button protocol live but make button id `0` a safe no-op until transmutation recipes are designed.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge 21.1.228, vanilla data components and menus, NeoForge GameTest, JSON resources, 32-by-32 PNG item art.

---

## File Map

**Create:**

- `src/main/java/com/fende/obesecat/item/TransmutationCubeItem.java` — item tooltip, right-click menu opening, and container-item nesting prohibition.
- `src/main/java/com/fende/obesecat/inventory/TransmutationCubeInventory.java` — 12-slot ItemStack-backed storage and insertion rule.
- `src/main/java/com/fende/obesecat/inventory/TransmutationCubeMenu.java` — slot layout, quick-move behavior, hand binding, validity, and inert button hook.
- `src/main/java/com/fende/obesecat/client/TransmutationCubeScreen.java` — vanilla-style screen, 3-by-4 slot drawing, and Transmutation button.
- `src/main/java/com/fende/obesecat/registry/ModMenus.java` — deferred menu-type registration.
- `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java` — server-side persistence, nesting, quick-move, validity, and button tests.
- `src/main/resources/assets/obesecat/models/item/transmutation_cube.json` — generated-item model.
- `src/main/resources/assets/obesecat/textures/item/transmutation_cube.png` — original 32-by-32 isometric cube sprite.
- `src/main/resources/data/obesecat/recipe/transmutation_cube.json` — shapeless survival recipe.

**Modify:**

- `build.gradle` — make the GameTest server return a usable Gradle result.
- `src/main/java/com/fende/obesecat/registry/ModItems.java` — register the rare cube item.
- `src/main/java/com/fende/obesecat/ObeseCatMod.java` — register menu types and expose the cube in Tools & Utilities.
- `src/main/java/com/fende/obesecat/ObeseCatModClient.java` — register the cube screen.
- `src/main/resources/assets/obesecat/lang/en_us.json` — item, caption, screen title, and button text.
- `docs/item-obtainment-report.md` — record the cube's survival recipe and acquisition status.

---

### Task 1: Persistent ItemStack Storage Foundation

**Files:**

- Create: `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java`
- Create: `src/main/java/com/fende/obesecat/inventory/TransmutationCubeInventory.java`
- Create: `src/main/java/com/fende/obesecat/item/TransmutationCubeItem.java`
- Modify: `src/main/java/com/fende/obesecat/registry/ModItems.java`
- Modify: `build.gradle`

- [ ] **Step 1: Write the failing persistence and nesting GameTest**

Create the initial test class:

```java
package com.fende.obesecat.gametest;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.inventory.TransmutationCubeInventory;
import com.fende.obesecat.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ObeseCatMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TransmutationCubeGameTests {
    private static final String TEMPLATE = "village/plains/houses/manhattan_bunker";

    @GameTest(template = TEMPLATE)
    public static void contentsPersistAndCubesCannotNest(GameTestHelper helper) {
        ItemStack cube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        TransmutationCubeInventory firstOpen = new TransmutationCubeInventory(cube);
        firstOpen.setItem(0, new ItemStack(Items.DIAMOND, 3));

        TransmutationCubeInventory reopened = new TransmutationCubeInventory(cube);
        helper.assertTrue(reopened.getContainerSize() == 12, "Cube must expose exactly 12 slots");
        helper.assertTrue(reopened.getItem(0).is(Items.DIAMOND), "Saved item type must persist");
        helper.assertTrue(reopened.getItem(0).getCount() == 3, "Saved item count must persist");
        helper.assertFalse(reopened.canPlaceItem(1, cube), "A cube inventory must reject another cube");
        helper.assertFalse(cube.canFitInsideContainerItems(), "A cube must reject generic container-item nesting");
        helper.succeed();
    }

    private TransmutationCubeGameTests() {
    }
}
```

- [ ] **Step 2: Run compilation to verify the test fails**

Run:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat compileJava
```

Expected: compilation fails because `TransmutationCubeInventory` and `ModItems.TRANSMUTATION_CUBE` do not exist.

- [ ] **Step 3: Implement the ItemStack-backed 12-slot inventory**

Create `TransmutationCubeInventory.java`:

```java
package com.fende.obesecat.inventory;

import com.fende.obesecat.item.TransmutationCubeItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class TransmutationCubeInventory extends SimpleContainer {
    public static final int SLOT_COUNT = 12;

    private final ItemStack cubeStack;

    public TransmutationCubeInventory(ItemStack cubeStack) {
        super(SLOT_COUNT);
        this.cubeStack = cubeStack;
        cubeStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(getItems());
        addListener(container -> save());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof TransmutationCubeItem);
    }

    private void save() {
        cubeStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }
}
```

This deliberately writes after every container mutation; it does not wait for screen closure, so moving or losing the opened cube cannot discard already-synchronized contents.

- [ ] **Step 4: Add the cube item shell and register it**

Create `TransmutationCubeItem.java` with the storage-safety behavior first:

```java
package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TransmutationCubeItem extends Item {
    public TransmutationCubeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.obesecat.transmutation_cube.caption").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean canFitInsideContainerItems(ItemStack stack) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }
}
```

Add the import and deferred item to `ModItems.java`:

```java
import com.fende.obesecat.item.TransmutationCubeItem;
```

```java
public static final DeferredItem<TransmutationCubeItem> TRANSMUTATION_CUBE = ITEMS.registerItem(
        "transmutation_cube",
        TransmutationCubeItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
);
```

- [ ] **Step 5: Make the GameTest server's exit status usable**

Extend the existing `gameTestServer` run configuration in `build.gradle`:

```groovy
gameTestServer {
    systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
    setForceExit false
}
```

- [ ] **Step 6: Run the persistence test and confirm it passes**

Run:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runGameTestServer
```

Expected: `contentsPersistAndCubesCannotNest` passes and Gradle ends with `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit the storage foundation**

```powershell
git add -- build.gradle src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java src/main/java/com/fende/obesecat/inventory/TransmutationCubeInventory.java src/main/java/com/fende/obesecat/item/TransmutationCubeItem.java src/main/java/com/fende/obesecat/registry/ModItems.java
git commit -m "feat: add transmutation cube storage"
```

---

### Task 2: Server Menu, Hand Binding, and Dummy Button

**Files:**

- Create: `src/main/java/com/fende/obesecat/inventory/TransmutationCubeMenu.java`
- Create: `src/main/java/com/fende/obesecat/registry/ModMenus.java`
- Modify: `src/main/java/com/fende/obesecat/item/TransmutationCubeItem.java`
- Modify: `src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java`
- Modify: `src/main/java/com/fende/obesecat/ObeseCatMod.java`

- [ ] **Step 1: Add failing menu behavior tests**

Append these imports and test method to `TransmutationCubeGameTests.java`:

```java
import com.fende.obesecat.inventory.TransmutationCubeMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
```

```java
@GameTest(template = TEMPLATE)
public static void menuRejectsQuickMoveAndButtonIsInert(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.CREATIVE);
    ItemStack openedCube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
    player.setItemInHand(InteractionHand.MAIN_HAND, openedCube);
    player.getInventory().setItem(9, new ItemStack(ModItems.TRANSMUTATION_CUBE.get()));

    TransmutationCubeMenu menu = new TransmutationCubeMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);
    menu.getCubeInventory().setItem(0, new ItemStack(Items.EMERALD, 2));
    ItemContainerContents before = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

    helper.assertTrue(menu.clickMenuButton(player, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID), "Known button id must be accepted");
    helper.assertTrue(before.equals(openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)), "Dummy button must not mutate contents");
    helper.assertFalse(menu.clickMenuButton(player, 99), "Unknown button ids must be rejected");
    helper.assertTrue(menu.quickMoveStack(player, TransmutationCubeInventory.SLOT_COUNT).isEmpty(), "Shift-clicked cube must not enter cube storage");
    helper.assertTrue(player.getInventory().getItem(9).is(ModItems.TRANSMUTATION_CUBE.get()), "Rejected nested cube must remain in player inventory");
    helper.assertTrue(menu.stillValid(player), "Menu must remain valid while the same held stack is present");

    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    helper.assertFalse(menu.stillValid(player), "Menu must become invalid if the opened cube leaves its hand");
    helper.succeed();
}
```

- [ ] **Step 2: Run compilation to verify the new test fails**

Run `\.\gradlew.bat compileJava` with the Java 21 environment from Task 1.

Expected: compilation fails because `TransmutationCubeMenu` does not exist.

- [ ] **Step 3: Register an extended menu type**

Create `ModMenus.java`:

```java
package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.inventory.TransmutationCubeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ObeseCatMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TransmutationCubeMenu>> TRANSMUTATION_CUBE = MENUS.register(
            "transmutation_cube",
            () -> IMenuTypeExtension.create(TransmutationCubeMenu::fromNetwork)
    );

    private ModMenus() {
    }
}
```

Register `ModMenus.MENUS` immediately after `ModItems.ITEMS` in the `ObeseCatMod` constructor:

```java
import com.fende.obesecat.registry.ModMenus;

ModMenus.MENUS.register(modEventBus);
```

- [ ] **Step 4: Implement the menu and slot layout**

Create `TransmutationCubeMenu.java`:

```java
package com.fende.obesecat.inventory;

import com.fende.obesecat.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TransmutationCubeMenu extends AbstractContainerMenu {
    public static final int TRANSMUTE_BUTTON_ID = 0;
    public static final int CUBE_SLOT_X = 62;
    public static final int CUBE_SLOT_Y = 20;
    public static final int PLAYER_SLOT_X = 8;
    public static final int PLAYER_SLOT_Y = 140;
    public static final int HOTBAR_SLOT_Y = 198;

    private final Container cubeInventory;
    private final InteractionHand openedHand;
    private final ItemStack openedStack;

    public static TransmutationCubeMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new TransmutationCubeMenu(containerId, playerInventory, buffer.readEnum(InteractionHand.class));
    }

    public TransmutationCubeMenu(int containerId, Inventory playerInventory, InteractionHand openedHand) {
        super(ModMenus.TRANSMUTATION_CUBE.get(), containerId);
        this.openedHand = openedHand;
        this.openedStack = playerInventory.player.getItemInHand(openedHand);
        this.cubeInventory = playerInventory.player.level().isClientSide
                ? new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT)
                : new TransmutationCubeInventory(openedStack);

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(cubeInventory, column + row * 3, CUBE_SLOT_X + column * 18, CUBE_SLOT_Y + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, PLAYER_SLOT_X + column * 18, PLAYER_SLOT_Y + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, PLAYER_SLOT_X + column * 18, HOTBAR_SLOT_Y));
        }
    }

    public Container getCubeInventory() {
        return cubeInventory;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().isClientSide || player.getItemInHand(openedHand) == openedStack;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id == TRANSMUTE_BUTTON_ID;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        int cubeSlots = TransmutationCubeInventory.SLOT_COUNT;
        boolean moved = index < cubeSlots
                ? moveItemStackTo(source, cubeSlots, slots.size(), true)
                : moveItemStackTo(source, 0, cubeSlots, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
```

The identity check in `stillValid` is intentional: a second visually identical cube is not accepted as the stack whose storage was opened.

- [ ] **Step 5: Open the menu from either hand**

Add these imports to `TransmutationCubeItem.java`:

```java
import com.fende.obesecat.inventory.TransmutationCubeMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
```

Add the right-click behavior:

```java
@Override
public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
    ItemStack stack = player.getItemInHand(usedHand);
    if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new TransmutationCubeMenu(containerId, inventory, usedHand),
                        Component.translatable("container.obesecat.transmutation_cube")
                ),
                buffer -> buffer.writeEnum(usedHand)
        );
    }
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
}
```

- [ ] **Step 6: Run the GameTests and confirm both pass**

Run `\.\gradlew.bat runGameTestServer` with Java 21.

Expected: both Transmutation Cube tests pass; the button leaves contents unchanged, nested quick-move fails, and the hand validity check changes to false after removal.

- [ ] **Step 7: Commit the menu behavior**

```powershell
git add -- src/main/java/com/fende/obesecat/ObeseCatMod.java src/main/java/com/fende/obesecat/gametest/TransmutationCubeGameTests.java src/main/java/com/fende/obesecat/inventory/TransmutationCubeMenu.java src/main/java/com/fende/obesecat/item/TransmutationCubeItem.java src/main/java/com/fende/obesecat/registry/ModMenus.java
git commit -m "feat: open transmutation cube menu"
```

---

### Task 3: Client Screen and Button Wiring

**Files:**

- Create: `src/main/java/com/fende/obesecat/client/TransmutationCubeScreen.java`
- Modify: `src/main/java/com/fende/obesecat/ObeseCatModClient.java`

- [ ] **Step 1: Establish a failing client compilation target**

Add these imports to `ObeseCatModClient.java`:

```java
import com.fende.obesecat.client.TransmutationCubeScreen;
import com.fende.obesecat.registry.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
```

Register the event listener in the client constructor and add its handler:

```java
modEventBus.addListener(this::registerMenuScreens);
```

```java
private void registerMenuScreens(RegisterMenuScreensEvent event) {
    event.register(ModMenus.TRANSMUTATION_CUBE.get(), TransmutationCubeScreen::new);
}
```

Run `\.\gradlew.bat compileJava`.

Expected: compilation fails because `TransmutationCubeScreen` does not exist.

- [ ] **Step 2: Implement the vanilla-style custom screen**

Create `TransmutationCubeScreen.java`:

```java
package com.fende.obesecat.client;

import com.fende.obesecat.inventory.TransmutationCubeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TransmutationCubeScreen extends AbstractContainerScreen<TransmutationCubeMenu> {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int PANEL_COLOR = 0xFF24221D;
    private static final int INNER_COLOR = 0xFF11100E;
    private static final int BRONZE_COLOR = 0xFF8A754D;

    public TransmutationCubeScreen(TransmutationCubeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 224;
        inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                Component.translatable("container.obesecat.transmutation_cube.transmute"),
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID);
                    }
                }
        ).bounds(leftPos + 43, topPos + 100, 90, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BRONZE_COLOR);
        guiGraphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, PANEL_COLOR);
        guiGraphics.fill(leftPos + 6, topPos + 16, leftPos + imageWidth - 6, topPos + 124, INNER_COLOR);
        guiGraphics.fill(leftPos + 6, topPos + 136, leftPos + imageWidth - 6, topPos + imageHeight - 6, INNER_COLOR);

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                guiGraphics.blitSprite(
                        SLOT_SPRITE,
                        leftPos + TransmutationCubeMenu.CUBE_SLOT_X - 1 + column * 18,
                        topPos + TransmutationCubeMenu.CUBE_SLOT_Y - 1 + row * 18,
                        18,
                        18
                );
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                guiGraphics.blitSprite(
                        SLOT_SPRITE,
                        leftPos + TransmutationCubeMenu.PLAYER_SLOT_X - 1 + column * 18,
                        topPos + TransmutationCubeMenu.PLAYER_SLOT_Y - 1 + row * 18,
                        18,
                        18
                );
            }
        }

        for (int column = 0; column < 9; column++) {
            guiGraphics.blitSprite(
                    SLOT_SPRITE,
                    leftPos + TransmutationCubeMenu.PLAYER_SLOT_X - 1 + column * 18,
                    topPos + TransmutationCubeMenu.HOTBAR_SLOT_Y - 1,
                    18,
                    18
            );
        }
    }
}
```

- [ ] **Step 3: Compile the client screen**

Run `\.\gradlew.bat compileJava` with Java 21.

Expected: `BUILD SUCCESSFUL` with the registered screen constructor and vanilla slot-sprite overloads resolving under the configured Parchment mappings.

- [ ] **Step 4: Commit the client screen**

```powershell
git add -- src/main/java/com/fende/obesecat/ObeseCatModClient.java src/main/java/com/fende/obesecat/client/TransmutationCubeScreen.java
git commit -m "feat: add transmutation cube screen"
```

---

### Task 4: Item Art, Recipe, Localization, and Obtainment Documentation

**Files:**

- Create: `src/main/resources/assets/obesecat/models/item/transmutation_cube.json`
- Create: `src/main/resources/assets/obesecat/textures/item/transmutation_cube.png`
- Create: `src/main/resources/data/obesecat/recipe/transmutation_cube.json`
- Modify: `src/main/resources/assets/obesecat/lang/en_us.json`
- Modify: `src/main/java/com/fende/obesecat/ObeseCatMod.java`
- Modify: `docs/item-obtainment-report.md`

- [ ] **Step 1: Generate the original 32-by-32 cube sprite**

Use the installed `imagegen` skill with the user's supplied Horadric Cube image as the sole visual reference and this prompt:

```text
Create one original Minecraft inventory item sprite on a transparent background, exactly square and designed to remain readable at 32x32 pixels. Isometric three-quarter view of a compact ancient transmutation cube: near-black carved stone faces, thick aged bronze-gold edge trim, circular occult relief motifs on the visible top and side panels, restrained highlights, strong pixel-art silhouette, no text, no frame, no cast shadow outside the object. Preserve the broad visual idea of the supplied reference without copying its exact carvings. Output a single centered icon with generous transparent padding.
```

Place the resulting PNG at `src/main/resources/assets/obesecat/textures/item/transmutation_cube.png`. Inspect it with the image viewer, confirm transparency and silhouette, and ensure the final file is exactly 32 by 32 pixels before continuing.

- [ ] **Step 2: Add the item model**

Create `transmutation_cube.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "obesecat:item/transmutation_cube"
  }
}
```

- [ ] **Step 3: Add the shapeless survival recipe**

Create `src/main/resources/data/obesecat/recipe/transmutation_cube.json`:

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "equipment",
  "ingredients": [
    {
      "item": "minecraft:chest"
    },
    {
      "item": "minecraft:crafting_table"
    },
    {
      "item": "obesecat:trinitite"
    }
  ],
  "result": {
    "id": "obesecat:transmutation_cube",
    "count": 1
  }
}
```

- [ ] **Step 4: Add exact player-facing strings**

Insert these keys into `en_us.json`, retaining valid commas and JSON syntax:

```json
"item.obesecat.transmutation_cube": "Transmutation Cube",
"item.obesecat.transmutation_cube.caption": "You have quite a treasure...",
"container.obesecat.transmutation_cube": "Transmutation Cube",
"container.obesecat.transmutation_cube.transmute": "Transmutation"
```

- [ ] **Step 5: Expose the cube in Tools & Utilities**

Add this line inside the existing `CreativeModeTabs.TOOLS_AND_UTILITIES` branch in `ObeseCatMod.addCreative`:

```java
event.accept(ModItems.TRANSMUTATION_CUBE.get());
```

Do not add the cube to Combat, Ingredients, or Spawn Eggs.

- [ ] **Step 6: Update the obtainment report**

In the `## Crafting Recipes` table in `docs/item-obtainment-report.md`, add:

```markdown
| Transmutation Cube | Shapeless | Chest + Crafting Table + Trinitite block | One Trinitite block plus common utility blocks |
```

In the acquisition/status section, state that `obesecat:transmutation_cube` is survival-obtainable through that recipe and that its 12-slot persistent inventory is available while transmutation outputs remain intentionally inactive.

- [ ] **Step 7: Validate resources and commit the complete item surface**

Run:

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat processResources
.\gradlew.bat build
```

Expected: both commands end with `BUILD SUCCESSFUL`; no malformed JSON, missing model, or missing texture is reported.

Commit:

```powershell
git add -- docs/item-obtainment-report.md src/main/java/com/fende/obesecat/ObeseCatMod.java src/main/resources/assets/obesecat/lang/en_us.json src/main/resources/assets/obesecat/models/item/transmutation_cube.json src/main/resources/assets/obesecat/textures/item/transmutation_cube.png src/main/resources/data/obesecat/recipe/transmutation_cube.json
git commit -m "feat: finish transmutation cube item"
```

---

### Task 5: End-to-End Verification

**Files:**

- Verify only; modify a file only if a test reveals a defect in the scoped implementation.

- [ ] **Step 1: Run automated server behavior tests**

```powershell
$env:JAVA_HOME = (Resolve-Path '.\jdk21\jdk-21.0.11+10').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runGameTestServer
```

Expected: all required GameTests pass and the process ends with `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run the release build**

```powershell
.\gradlew.bat clean build
```

Expected: `BUILD SUCCESSFUL` and a mod jar appears under `build/libs/` without changing versioned artifact naming, because this is feature work rather than a publish step.

- [ ] **Step 3: Verify packaged resources**

```powershell
jar tf (Get-ChildItem -LiteralPath 'build\libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName | Select-String 'transmutation_cube'
```

Expected output includes the item texture, item model, recipe, and Transmutation Cube classes.

- [ ] **Step 4: Run the manual in-game checklist**

Launch the client with `\.\gradlew.bat runClient`, then verify each item:

1. Craft the cube with one Chest, one Crafting Table, and one Trinitite in multiple shapeless arrangements.
2. Confirm rare name coloring and exact tooltip `You have quite a treasure...`.
3. Confirm the 32-by-32 sprite is crisp in inventory, in hand, and when dropped.
4. Open from main hand and offhand.
5. Confirm exactly 12 slots arranged three wide by four high.
6. Insert stacked and unstacked ordinary items; close and reopen.
7. Drop and pick up a filled cube; confirm exact persistence.
8. Transfer ordinary items both normally and with shift-click.
9. Attempt cube nesting by normal placement and shift-click; both must fail.
10. Press Transmutation; confirm no item changes, drops, sounds, or errors.
11. Move the opened cube out of its original hand slot; confirm the menu closes safely.
12. Repeat persistence and button checks on a dedicated server or two-client local server.

- [ ] **Step 5: Review scope and working tree**

```powershell
git status --short
git diff --check HEAD~3..HEAD
git log -4 --oneline
```

Expected: only the planned Transmutation Cube changes and the user's pre-existing unrelated untracked files are present; diff check reports no whitespace errors; the three feature commits are visible.
