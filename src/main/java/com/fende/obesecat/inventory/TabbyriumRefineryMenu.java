package com.fende.obesecat.inventory;

import com.fende.obesecat.block.entity.TabbyriumRefineryBlockEntity;
import com.fende.obesecat.energy.TabbyriumEnergy;
import com.fende.obesecat.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TabbyriumRefineryMenu extends AbstractContainerMenu {
    public static final int INPUT_X = 35;
    public static final int INPUT_Y = 50;
    public static final int PLAYER_X = 20;
    public static final int PLAYER_Y = 177;
    public static final int HOTBAR_Y = 235;
    public static final int ITEM_SIDE_BUTTON_BASE = 100;
    public static final int POWER_SIDE_BUTTON_BASE = 200;

    private final Container refinery;
    private final ContainerData data;
    private final TabbyriumRefineryBlockEntity blockEntity;

    public static TabbyriumRefineryMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        buffer.readBlockPos();
        return new TabbyriumRefineryMenu(id, inventory, new SimpleContainer(1), new SimpleContainerData(16), null);
    }

    public TabbyriumRefineryMenu(int id, Inventory inventory, Container refinery, ContainerData data, TabbyriumRefineryBlockEntity blockEntity) {
        super(ModMenus.TABBYRIUM_REFINERY.get(), id);
        this.refinery = refinery;
        this.data = data;
        this.blockEntity = blockEntity;
        checkContainerSize(refinery, 1);
        checkContainerDataCount(data, 16);
        refinery.startOpen(inventory.player);

        addSlot(new Slot(refinery, 0, INPUT_X, INPUT_Y) {
            @Override public boolean mayPlace(ItemStack stack) { return TabbyriumEnergy.getFuelEnergy(stack) > 0; }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, PLAYER_X + col * 18, PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, PLAYER_X + col * 18, HOTBAR_Y));
        }
        addDataSlots(data);
    }

    public int getEnergy() { return data.get(0); }
    public int getCapacity() { return data.get(1); }
    public int getProgress() { return data.get(2); }
    public int getProcessTime() { return data.get(3); }
    public boolean isItemInputEnabled(int directionIndex) { return data.get(4 + directionIndex) != 0; }
    public boolean isPowerOutputEnabled(int directionIndex) { return data.get(10 + directionIndex) != 0; }
    public int getEnergyBarHeight(int height) { return getCapacity() <= 0 ? 0 : getEnergy() * height / getCapacity(); }
    public int getProgressWidth(int width) { return getProcessTime() <= 0 ? 0 : getProgress() * width / getProcessTime(); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity == null) return false;
        if (id >= ITEM_SIDE_BUTTON_BASE && id < ITEM_SIDE_BUTTON_BASE + 6) {
            blockEntity.toggleItemInput(id - ITEM_SIDE_BUTTON_BASE);
            return true;
        }
        if (id >= POWER_SIDE_BUTTON_BASE && id < POWER_SIDE_BUTTON_BASE + 6) {
            blockEntity.togglePowerOutput(id - POWER_SIDE_BUTTON_BASE);
            return true;
        }
        return false;
    }

    @Override public boolean stillValid(Player player) { return refinery.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); refinery.stopOpen(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index == 0) {
            if (!moveItemStackTo(source, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (TabbyriumEnergy.getFuelEnergy(source) > 0) {
            if (!moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
}
