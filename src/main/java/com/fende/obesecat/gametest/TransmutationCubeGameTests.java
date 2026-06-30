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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
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
                        new TransmutationIngredient(ModItems.PACO.get(), 1),
                        new TransmutationIngredient(ModItems.OPPENHEIMERS_HAT.get(), 1)
                ),
                new ItemStack(ModItems.J_ROBERT_PACOHEIMER.get())
        );

        SimpleContainer contents = new SimpleContainer(TransmutationCubeInventory.SLOT_COUNT);
        contents.setItem(11, new ItemStack(ModItems.PACO.get()));
        contents.setItem(2, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        helper.assertTrue(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "Recipe must match items in arbitrary cube slots");

        contents.clearContent();
        contents.setItem(0, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        contents.setItem(10, new ItemStack(ModItems.PACO.get()));
        helper.assertTrue(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "Recipe must ignore input ordering");

        contents.setItem(5, new ItemStack(Items.DIAMOND));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "An extra item must reject an otherwise valid recipe");

        contents.clearContent();
        contents.setItem(0, new ItemStack(ModItems.PACO.get()));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "A missing ingredient must reject the recipe");

        contents.setItem(1, new ItemStack(Items.LEATHER_HELMET));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "A wrong item must reject the recipe");

        contents.setItem(2, new ItemStack(ModItems.PACO.get()));
        contents.setItem(1, new ItemStack(ModItems.OPPENHEIMERS_HAT.get()));
        helper.assertFalse(recipe.matches(TransmutationInput.copyOf(contents), helper.getLevel()),
                "A duplicate ingredient must reject the exact recipe");
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
    public static void menuRejectsQuickMoveAndButtonIsInert(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack openedCube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, openedCube);
        player.getInventory().setItem(9, new ItemStack(ModItems.TRANSMUTATION_CUBE.get()));

        TransmutationCubeMenu menu = new TransmutationCubeMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);
        menu.getCubeInventory().setItem(0, new ItemStack(Items.EMERALD, 2));
        ItemContainerContents before = openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        helper.assertTrue(
                menu.clickMenuButton(player, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID),
                "Known button id must be accepted"
        );
        helper.assertTrue(
                before.equals(openedCube.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)),
                "Dummy button must not mutate contents"
        );
        helper.assertFalse(menu.clickMenuButton(player, 99), "Unknown button ids must be rejected");
        helper.assertTrue(
                menu.quickMoveStack(player, TransmutationCubeInventory.SLOT_COUNT).isEmpty(),
                "Shift-clicked cube must not enter cube storage"
        );
        helper.assertTrue(
                player.getInventory().getItem(9).is(ModItems.TRANSMUTATION_CUBE.get()),
                "Rejected nested cube must remain in player inventory"
        );
        helper.assertTrue(menu.stillValid(player), "Menu must remain valid while the same held stack is present");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.assertFalse(menu.stillValid(player), "Menu must become invalid if the opened cube leaves its hand");
        helper.succeed();
    }

    private TransmutationCubeGameTests() {
    }
}
