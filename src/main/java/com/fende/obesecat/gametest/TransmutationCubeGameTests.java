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
        ItemStack anotherCube = new ItemStack(ModItems.TRANSMUTATION_CUBE.get());
        helper.assertFalse(reopened.canPlaceItem(1, anotherCube), "A cube inventory must reject another cube");
        helper.assertFalse(cube.canFitInsideContainerItems(), "A cube must reject generic container-item nesting");

        reopened.removeItemNoUpdate(0);
        TransmutationCubeInventory reopenedAfterRemoval = new TransmutationCubeInventory(cube);
        helper.assertTrue(reopenedAfterRemoval.getItem(0).isEmpty(), "Removal without update must persist");
        helper.succeed();
    }

    private TransmutationCubeGameTests() {
    }
}
