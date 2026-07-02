package com.fende.obesecat.gametest;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.inventory.TransmutationCubeInventory;
import com.fende.obesecat.inventory.TransmutationCubeMenu;
import com.fende.obesecat.inventory.TransmutationCubeSlot;
import com.fende.obesecat.item.TransmutationCubeItem;
import com.fende.obesecat.recipe.TransmutationIngredient;
import com.fende.obesecat.recipe.TransmutationInput;
import com.fende.obesecat.recipe.TransmutationRecipe;
import com.fende.obesecat.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ObeseCatMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TransmutationCubeGameTests {
    private static final String TEMPLATE = "village/plains/houses/manhattan_bunker";

    @GameTest(template = TEMPLATE)
    public static void transmutationRecipeMatchesExactUnorderedContents(GameTestHelper helper) {
        TransmutationRecipe recipe = new TransmutationRecipe(
                List.of(
                        new TransmutationIngredient(ModItems.ASSAULT_PACO.get(), 1),
                        new TransmutationIngredient(Items.SPYGLASS, 1)
                ),
                new ItemStack(ModItems.SNIPER_PACO.get())
        );

        SimpleContainer contents = new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT);
        contents.setItem(11, new ItemStack(ModItems.ASSAULT_PACO.get()));
        contents.setItem(2, new ItemStack(Items.SPYGLASS));
        helper.assertTrue(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "Recipe must match items in arbitrary cube slots");

        contents.clearContent();
        contents.setItem(0, new ItemStack(Items.SPYGLASS));
        contents.setItem(10, new ItemStack(ModItems.ASSAULT_PACO.get()));
        helper.assertTrue(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "Recipe must ignore input ordering");

        contents.setItem(5, new ItemStack(Items.DIAMOND));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "An extra item must reject an otherwise valid recipe");

        contents.clearContent();
        contents.setItem(0, new ItemStack(ModItems.ASSAULT_PACO.get()));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "A missing ingredient must reject the recipe");

        contents.setItem(1, new ItemStack(Items.LEATHER_HELMET));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "A wrong item must reject the recipe");

        contents.setItem(2, new ItemStack(ModItems.ASSAULT_PACO.get()));
        contents.setItem(1, new ItemStack(Items.SPYGLASS));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "A duplicate ingredient must reject the exact recipe");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void hellhoundPacoTransmutationRecipeIsLoaded(GameTestHelper helper) {
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                ObeseCatMod.MOD_ID,
                "hellhound_paco_transmutation"
        );
        helper.assertTrue(
                helper.getLevel().getRecipeManager().byKey(recipeId).isPresent(),
                "The Hellhound Paco transmutation recipe must be loaded"
        );

        TransmutationRecipe recipe = helper.getLevel().getRecipeManager()
                .byKey(recipeId)
                .map(recipeHolder -> (TransmutationRecipe) recipeHolder.value())
                .orElseThrow();

        SimpleContainer contents = new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT);
        contents.setItem(4, new ItemStack(ModItems.PACO.get()));
        contents.setItem(9, new ItemStack(Items.BLAZE_POWDER));
        helper.assertTrue(
                recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "Paco plus blaze powder must match the Hellhound recipe in any cube slots"
        );
        helper.assertTrue(
                recipe.assemble(TransmutationInput.copyOf(contents), helper.getLevel().registryAccess())
                        .is(ModItems.HELLHOUND_PACO.get()),
                "The Hellhound recipe must transmute into Hellhound Paco"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void cowLevelPortalTransmutationRecipeIsLoaded(GameTestHelper helper) {
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                ObeseCatMod.MOD_ID,
                "cow_level_portal_transmutation"
        );
        helper.assertTrue(
                helper.getLevel().getRecipeManager().byKey(recipeId).isPresent(),
                "The Cow Level Portal transmutation recipe must be loaded"
        );

        TransmutationRecipe recipe = helper.getLevel().getRecipeManager()
                .byKey(recipeId)
                .map(recipeHolder -> (TransmutationRecipe) recipeHolder.value())
                .orElseThrow();

        SimpleContainer contents = new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT);
        contents.setItem(1, new ItemStack(ModItems.TP_TOME.get()));
        contents.setItem(8, new ItemStack(ModItems.VIRTS_LEG.get()));
        helper.assertTrue(
                recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "Tome of Town Portal plus Virt's Leg must match the Cow Level Portal recipe in any cube slots"
        );
        helper.assertTrue(
                recipe.assemble(TransmutationInput.copyOf(contents), helper.getLevel().registryAccess())
                        .is(ModItems.COW_LEVEL_PORTAL.get()),
                "The Cow Level Portal recipe must transmute into Cow Level Portal"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void contentsPersistAndCubesCannotNest(GameTestHelper helper) {
        ItemStack cube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        TransmutationCubeInventory firstOpen = new TransmutationCubeInventory(cube);
        firstOpen.setItem(0, new ItemStack(Items.DIAMOND, 3));

        TransmutationCubeInventory reopened = new TransmutationCubeInventory(cube);
        helper.assertTrue(reopened.getContainerSize() == 12, "Cube must expose exactly 12 slots");
        helper.assertTrue(reopened.getItem(0).is(Items.DIAMOND), "Saved item type must persist");
        helper.assertTrue(reopened.getItem(0).getCount() == 3, "Saved item count must persist");
        ItemStack anotherCube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        helper.assertFalse(reopened.canPlaceItem(1, anotherCube), "A cube inventory must reject another cube");
        TransmutationCubeSlot slot = new TransmutationCubeSlot(reopened, 1, 0, 0);
        helper.assertFalse(slot.mayPlace(anotherCube), "A cube slot must reject another cube");
        SimpleContainer clientInventory = new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT);
        TransmutationCubeSlot clientSlot = new TransmutationCubeSlot(clientInventory, 1, 0, 0);
        helper.assertFalse(clientSlot.mayPlace(anotherCube), "A client cube slot must reject another cube");
        helper.assertFalse(cube.canFitInsideContainerItems(), "A cube must reject generic container-item nesting");
        helper.assertFalse(
                ((TransmutationCubeItem) cube.getItem()).canFitInsideContainerItems(),
                "A cube item must reject generic container-item nesting through the deprecated API"
        );

        reopened.removeItemNoUpdate(0);
        TransmutationCubeInventory reopenedAfterRemoval = new TransmutationCubeInventory(cube);
        helper.assertTrue(reopenedAfterRemoval.getItem(0).isEmpty(), "Removal without update must persist");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void menuExecutesTransmutationAndPersistsResult(GameTestHelper helper) {
        var serverPlayer = helper.makeMockServerPlayerInLevel();
        ItemStack openedCube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, openedCube);

        TransmutationCubeMenu menu = new TransmutationCubeMenu(1, serverPlayer.getInventory(), InteractionHand.MAIN_HAND);
        menu.getCubeInventory().setItem(2, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        menu.getCubeInventory().setItem(11, new ItemStack(ModItems.PACO.get()));

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                ObeseCatMod.MOD_ID,
                "j_robert_pacoheimer_transmutation"
        );
        helper.assertTrue(
                helper.getLevel().getRecipeManager().byKey(recipeId).isPresent(),
                "The transmutation recipe must be loaded"
        );
        helper.assertTrue(
                helper.getLevel().getRecipeManager()
                        .byKey(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "sniper_paco"))
                        .isPresent(),
                "The sniper crafting recipe must be loaded"
        );
        helper.assertTrue(
                helper.getLevel().getRecipeManager()
                        .byKey(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "sniper_paco_decraft"))
                        .isPresent(),
                "The sniper deconstruction recipe must be loaded"
        );
        helper.assertFalse(
                helper.getLevel().getRecipeManager()
                        .byKey(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "j_robert_pacoheimer"))
                        .isPresent(),
                "The old crafting recipe must be removed"
        );

        helper.assertTrue(menu.clickMenuButton(serverPlayer, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "Known button id must be accepted on the server");
        ItemContainerContents afterTransmute = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.assertTrue(
                afterTransmute.equals(contentsWithSlots(
                        new ItemStack(ModItems.J_ROBERT_PACOHEIMER.get()),
                        2
                )),
                "Successful transmutation must leave the result in the lowest occupied slot"
        );
        TransmutationCubeInventory reopened = new TransmutationCubeInventory(openedCube);
        helper.assertTrue(
                reopened.getItem(2).is(ModItems.J_ROBERT_PACOHEIMER.get()),
                "Reopening the same cube stack must preserve the result"
        );
        helper.assertTrue(reopened.getItem(0).isEmpty(), "Reopening must not recreate consumed ingredients");
        helper.assertTrue(reopened.getItem(11).isEmpty(), "Reopening must keep the remaining slots empty");

        serverPlayer.getInventory().setItem(9, new ItemStack(ModItems.TRANSMUTATION_CUBE.get()));
        helper.assertTrue(
                menu.quickMoveStack(serverPlayer, TransmutationCubeInventory.SLOT_COUNT).isEmpty(),
                "Shift-clicked cube must not enter cube storage"
        );
        helper.assertTrue(
                serverPlayer.getInventory().getItem(9).is(ModItems.TRANSMUTATION_CUBE.get()),
                "Rejected nested cube must remain in player inventory"
        );

        ItemStack reversedCube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, reversedCube);
        TransmutationCubeMenu reversedMenu = new TransmutationCubeMenu(2, serverPlayer.getInventory(), InteractionHand.MAIN_HAND);
        reversedMenu.getCubeInventory().setItem(0, new ItemStack(ModItems.PACO.get()));
        reversedMenu.getCubeInventory().setItem(10, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        helper.assertTrue(
                reversedMenu.clickMenuButton(serverPlayer, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "The recipe must match regardless of ingredient ordering"
        );
        helper.assertTrue(
                reversedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                        .equals(contentsWithSlots(new ItemStack(ModItems.J_ROBERT_PACOHEIMER.get()), 0)),
                "The result must land in the lowest occupied slot"
        );

        serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, openedCube);
        menu.getCubeInventory().clearContent();
        menu.getCubeInventory().setItem(2, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        menu.getCubeInventory().setItem(11, new ItemStack(ModItems.PACO.get()));
        ItemContainerContents beforeInvalidClick = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.assertFalse(menu.clickMenuButton(serverPlayer, 99), "Unknown button ids must be rejected");
        helper.assertTrue(
                beforeInvalidClick.equals(openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)),
                "Unknown button ids must not mutate contents"
        );

        menu.getCubeInventory().clearContent();
        menu.getCubeInventory().setItem(2, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        menu.getCubeInventory().setItem(11, new ItemStack(ModItems.PACO.get()));
        menu.getCubeInventory().setItem(5, new ItemStack(Items.DIAMOND));
        ItemContainerContents beforeExtra = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.assertTrue(menu.clickMenuButton(serverPlayer, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "Server transmute clicks should still be accepted when the recipe does not match");
        helper.assertTrue(
                beforeExtra.equals(openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)),
                "An extra item must prevent mutation"
        );

        menu.getCubeInventory().clearContent();
        menu.getCubeInventory().setItem(0, new ItemStack(ModItems.PACO.get()));
        ItemContainerContents beforeMissing = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.assertTrue(menu.clickMenuButton(serverPlayer, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "Server transmute clicks should still be accepted when an ingredient is missing");
        helper.assertTrue(
                beforeMissing.equals(openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)),
                "A missing ingredient must prevent mutation"
        );

        menu.getCubeInventory().clearContent();
        menu.getCubeInventory().setItem(0, new ItemStack(ModItems.PACO.get()));
        menu.getCubeInventory().setItem(1, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        menu.getCubeInventory().setItem(2, new ItemStack(Items.LEATHER_HELMET));
        ItemContainerContents beforeWrong = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.assertTrue(menu.clickMenuButton(serverPlayer, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "Server transmute clicks should still be accepted when the wrong item is present");
        helper.assertTrue(
                beforeWrong.equals(openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)),
                "A wrong item must prevent mutation"
        );

        menu.getCubeInventory().clearContent();
        menu.getCubeInventory().setItem(0, new ItemStack(ModItems.PACO.get()));
        menu.getCubeInventory().setItem(1, new ItemStack(ModItems.PACO.get()));
        menu.getCubeInventory().setItem(2, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        ItemContainerContents beforeDuplicate = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.assertTrue(menu.clickMenuButton(serverPlayer, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "Server transmute clicks should still be accepted when duplicate items are present");
        helper.assertTrue(
                beforeDuplicate.equals(openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)),
                "A duplicate ingredient must prevent mutation"
        );

        Player clientSidePlayer = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack clientCube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        clientSidePlayer.setItemInHand(InteractionHand.MAIN_HAND, clientCube);
        TransmutationCubeMenu clientMenu = new TransmutationCubeMenu(3, clientSidePlayer.getInventory(), InteractionHand.MAIN_HAND);
        clientMenu.getCubeInventory().setItem(0, new ItemStack(Items.EMERALD, 2));
        ItemContainerContents clientBefore = clientCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.assertTrue(
                clientMenu.clickMenuButton(clientSidePlayer, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "Known button id must be accepted for non-server callers"
        );
        helper.assertTrue(
                clientBefore.equals(clientCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)),
                "Non-server callers must not mutate cube contents"
        );
        helper.assertFalse(clientMenu.clickMenuButton(clientSidePlayer, 99), "Unknown button ids must be rejected");
        helper.assertTrue(menu.stillValid(serverPlayer), "Menu must remain valid while the same held stack is present");

        serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.assertFalse(menu.stillValid(serverPlayer), "Menu must become invalid if the opened cube leaves its hand");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void sniperPacoLeavesSpyglassInCraftingGrid(GameTestHelper helper) {
        ItemStack sniperPaco = new ItemStack(ModItems.SNIPER_PACO.get());
        helper.assertTrue(
                sniperPaco.getItem().hasCraftingRemainingItem(),
                "Sniper Paco should leave a crafting remainder"
        );
        helper.assertTrue(
                sniperPaco.getItem().getCraftingRemainingItem(sniperPaco).is(Items.SPYGLASS),
                "Sniper Paco should return a spyglass when used in crafting"
        );
        helper.succeed();
    }

    private static ItemContainerContents contentsWithSlots(ItemStack item, int slot) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(TransmutationCubeInventory.SLOT_COUNT, ItemStack.EMPTY);
        stacks.set(slot, item);
        return ItemContainerContents.fromItems(stacks);
    }

    private TransmutationCubeGameTests() {
    }
}
