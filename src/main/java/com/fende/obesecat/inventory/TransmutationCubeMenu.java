package com.fende.obesecat.inventory;

import com.fende.obesecat.registry.ModMenus;
import com.fende.obesecat.recipe.TransmutationInput;
import com.fende.obesecat.recipe.TransmutationRecipe;
import com.fende.obesecat.registry.ModRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.NonNullList;

import java.util.Optional;

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

    public static TransmutationCubeMenu fromNetwork(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buffer
    ) {
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
                addSlot(new TransmutationCubeSlot(
                        cubeInventory,
                        column + row * 3,
                        CUBE_SLOT_X + column * 18,
                        CUBE_SLOT_Y + row * 18
                ));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        PLAYER_SLOT_X + column * 18,
                        PLAYER_SLOT_Y + row * 18
                ));
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
        if (id != TRANSMUTE_BUTTON_ID) {
            return false;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }
        if (!stillValid(serverPlayer) || !(cubeInventory instanceof TransmutationCubeInventory persistentInventory)) {
            return false;
        }

        TransmutationInput input = TransmutationInput.copyOf(cubeInventory);
        Optional<RecipeHolder<TransmutationRecipe>> match = serverPlayer.level().getRecipeManager()
                .getRecipeFor(ModRecipeTypes.TRANSMUTATION.get(), input, serverPlayer.level());
        if (match.isEmpty()) {
            return true;
        }

        ItemStack output = match.get().value().assemble(input, serverPlayer.level().registryAccess());
        int outputSlot = input.lowestOccupiedSlot();
        if (output.isEmpty() || outputSlot < 0) {
            return true;
        }

        NonNullList<ItemStack> replacement = NonNullList.withSize(TransmutationCubeInventory.SLOT_COUNT, ItemStack.EMPTY);
        replacement.set(outputSlot, output);
        persistentInventory.replaceContents(replacement);
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

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
