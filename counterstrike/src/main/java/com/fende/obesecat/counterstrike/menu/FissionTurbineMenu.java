package com.fende.obesecat.counterstrike.menu;

import com.fende.obesecat.counterstrike.block.entity.FissionTurbineCoreBlockEntity;
import com.fende.obesecat.counterstrike.registry.CSMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FissionTurbineMenu extends AbstractContainerMenu {
    private final BlockPos corePos;
    private final ContainerData data;

    public static FissionTurbineMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new FissionTurbineMenu(id, inventory, pos, new SimpleContainerData(6), new ItemStackHandler(1));
    }

    public FissionTurbineMenu(int id, Inventory inventory, BlockPos corePos, ContainerData data, IItemHandler fuelHandler) {
        super(CSMenus.FISSION_TURBINE.get(), id);
        this.corePos = corePos;
        this.data = data;
        checkContainerDataCount(data, 6);
        addDataSlots(data);

        // Fuel Slot at (80, 52)
        this.addSlot(new SlotItemHandler(fuelHandler, 0, 80, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return FissionTurbineCoreBlockEntity.getFuelBurnTime(stack) > 0;
            }
        });

        // Player Inventory
        int startX = 8;
        int startY = 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        // Hotbar
        int hotbarY = 142;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, startX + col * 18, hotbarY));
        }
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    public int getBurnTime() {
        return data.get(0);
    }

    public int getMaxBurnTime() {
        return data.get(1);
    }

    public int getPowerGenerated() {
        return data.get(2);
    }

    public int getPowerConsumed() {
        return data.get(3);
    }

    public boolean isLowPower() {
        return data.get(4) == 1;
    }

    public Direction getFacing() {
        return Direction.from2DDataValue(data.get(5));
    }

    public float getBurnProgress() {
        int max = getMaxBurnTime();
        if (max <= 0) return 0.0f;
        return (float) getBurnTime() / (float) max;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 0) {
                // From Fuel Slot to Player Inventory
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From Player Inventory to Fuel Slot
                if (FissionTurbineCoreBlockEntity.getFuelBurnTime(slotStack) > 0) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.moveItemStackTo(slotStack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5) <= 64.0;
    }
}
