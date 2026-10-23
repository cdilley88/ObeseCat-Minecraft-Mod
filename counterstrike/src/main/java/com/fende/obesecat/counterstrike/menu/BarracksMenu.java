package com.fende.obesecat.counterstrike.menu;

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

public class BarracksMenu extends AbstractContainerMenu {
    private final BlockPos corePos;
    private final ContainerData data;

    public static BarracksMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new BarracksMenu(id, inventory, pos, new SimpleContainerData(7), new ItemStackHandler(5));
    }

    public BarracksMenu(int id, Inventory inventory, BlockPos corePos, ContainerData data, IItemHandler hopperHandler) {
        super(CSMenus.BARRACKS.get(), id);
        this.corePos = corePos;
        this.data = data;
        checkContainerDataCount(data, 7);
        addDataSlots(data);

        // 4 Input Hopper Slots (0..3)
        this.addSlot(new SlotItemHandler(hopperHandler, 0, 18, 104));
        this.addSlot(new SlotItemHandler(hopperHandler, 1, 36, 104));
        this.addSlot(new SlotItemHandler(hopperHandler, 2, 54, 104));
        this.addSlot(new SlotItemHandler(hopperHandler, 3, 72, 104));

        // 1 Output Weapon Slot (4)
        this.addSlot(new SlotItemHandler(hopperHandler, 4, 138, 104) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player Inventory (3 rows of 9) - y = 130
        int startX = 8;
        int startY = 130;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        // Hotbar - y = 188
        int hotbarY = 188;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, startX + col * 18, hotbarY));
        }
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    public Direction getFacing() {
        return Direction.from2DDataValue(data.get(0));
    }

    public int getProgress() {
        return data.get(1);
    }

    public int getMaxProgress() {
        return data.get(2);
    }

    public int getPowerGenerated() {
        return data.get(3);
    }

    public int getPowerConsumed() {
        return data.get(4);
    }

    public boolean isLowPower() {
        return data.get(5) == 1;
    }

    public boolean isConstructing() {
        return data.get(6) == 1;
    }

    public float getProgressRatio() {
        int max = getMaxProgress();
        if (max <= 0) return 0.0f;
        return (float) getProgress() / (float) max;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < 5) {
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 0, 4, false)) {
                    if (index < 32) {
                        if (!this.moveItemStackTo(slotStack, 32, 41, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, 5, 32, false)) {
                        return ItemStack.EMPTY;
                    }
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
