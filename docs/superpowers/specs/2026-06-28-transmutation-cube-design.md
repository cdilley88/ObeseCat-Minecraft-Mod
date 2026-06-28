# Transmutation Cube Design

## Goal

Add a portable Transmutation Cube to ObeseCat. In this release, the cube is a fully functional persistent 12-slot container with a visible but intentionally inactive Transmutation button. Actual transmutation recipes and outputs are reserved for a later feature.

## Player Experience

- The item is named **Transmutation Cube**.
- It has `RARE` rarity, is limited to one item per stack, and is not placeable.
- Its yellow tooltip caption is exactly: `You have quite a treasure...`
- Right-clicking the cube from either hand opens its container screen.
- The screen title is `Transmutation Cube`.
- The cube inventory contains 12 slots arranged three columns wide by four rows high.
- The normal player inventory and hotbar appear below the cube inventory.
- A button labeled `Transmutation` is present in the screen. Pressing it is accepted safely but performs no action in this release.
- The cube appears in the Tools & Utilities creative tab.

## Visual Direction

The item uses an isometric Minecraft-style sprite based on the supplied Horadric Cube reference. It should read clearly as a three-faced cube at item-icon size, with dark carved stone panels, aged bronze-gold edge trim, and circular occult motifs. The result should be an original pixel-art interpretation suited to the existing ObeseCat item textures, not a direct copy of the source image.

The container screen keeps the familiar vanilla inventory visual language. Its custom texture needs only enough ornament to connect it to the item: dark stone or metal framing, muted bronze accents, the 3-by-4 slot grid, and the Transmutation button. Readability and obvious slot boundaries take priority over decoration.

## Crafting and Obtainment

The cube has a shapeless crafting recipe containing exactly:

- one vanilla Chest;
- one vanilla Crafting Table; and
- one ObeseCat Trinitite block item.

The output is one Transmutation Cube. This recipe is its survival acquisition path and should be added to the item obtainment report.

## Architecture

### Registration

Register `obesecat:transmutation_cube` as a dedicated item class. Register a matching menu type and client screen. Add the item model, texture, translations, recipe, creative-tab entry, and obtainment documentation through the repo's existing registries and resource conventions.

### Persistent Inventory

Each physical cube stores its own 12-slot contents directly on its ItemStack using Minecraft's item container data component. Contents therefore travel with the cube when it is moved between inventory slots, dropped and picked up, or transferred between players.

The server is authoritative. Opening the cube creates a server menu backed by the held cube's stored contents; changes are synchronized to the client and written back to that same ItemStack. Closing and reopening the cube must preserve all contents.

### Menu Validity

The menu records the hand and inventory location associated with the opened cube. It remains valid only while the same Transmutation Cube ItemStack is still available at that location. If the cube is replaced, moved away, or otherwise becomes unavailable, the menu closes instead of writing contents into a different item.

Normal clicking and shift-clicking move items between the cube and player inventory. Cube slots reject all Transmutation Cube items, preventing direct or indirect cube nesting. Shift-click logic must enforce the same rejection.

### Transmutation Hook

The screen sends the standard menu button action for the Transmutation button. The server menu recognizes the button identifier and returns safely without reading, consuming, replacing, or dropping any item. This creates a stable extension point for later recipe matching while keeping this release deliberately inert.

## Failure Handling

- If the held cube cannot be identified when the menu is opened, do not open the menu.
- If the cube becomes invalid while its menu is open, close the menu without assigning its stored contents to another stack.
- Reject cube items at slot insertion time and during quick-move operations.
- Treat unknown menu button identifiers as invalid and make no inventory changes.
- Keep all inventory mutation server-side so a client cannot create or consume items by sending arbitrary button or slot actions.

## Verification

Build verification:

- Run the project Gradle build using the workspace Java 21 toolchain.
- Confirm all Java registrations and JSON resources load without errors.

In-game verification:

- Craft one cube using a Chest, Crafting Table, and Trinitite in any crafting-grid arrangement.
- Confirm the item name, rare name color, exact caption, item sprite, and Tools & Utilities creative entry.
- Open the cube from the main hand and offhand.
- Confirm the cube grid is three slots wide and four slots high and that the player inventory is usable.
- Add ordinary items, close the menu, reopen it, and confirm exact persistence.
- Drop and pick up the filled cube, then confirm contents still persist.
- Move items both normally and with shift-click.
- Attempt to insert another cube using normal clicks and shift-click; both must fail.
- Press Transmutation and confirm it changes nothing and causes no errors.
- Move or replace the opened cube and confirm the menu closes safely.
- Verify behavior in a client/server environment, not only an integrated client.

## Explicitly Deferred

- Transmutation recipes, matching rules, inputs, outputs, costs, effects, sounds, and animations.
- Placing the cube as a world block.
- Cube nesting of any kind.
- Player-global or externally keyed cube storage.
