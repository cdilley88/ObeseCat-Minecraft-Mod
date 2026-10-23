package com.fende.obesecat.counterstrike.menu;

import com.fende.obesecat.counterstrike.registry.CSMenus;
import com.fende.obesecat.counterstrike.resource.ResourceValues;
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

public class ConstructionYardMenu extends AbstractContainerMenu {
    private final BlockPos corePos;
    private final ContainerData data;
    private final SlotItemHandler[] hopperSlots = new SlotItemHandler[5];
    private final SlotItemHandler outputSlot;

    public static ConstructionYardMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new ConstructionYardMenu(id, inventory, pos, new SimpleContainerData(12), new ItemStackHandler(6));
    }

    private int currentTab = 0;

    public ConstructionYardMenu(int id, Inventory inventory, BlockPos corePos, ContainerData data, IItemHandler hopperHandler) {
        super(CSMenus.CONSTRUCTION_YARD.get(), id);
        this.corePos = corePos;
        this.data = data;
        checkContainerDataCount(data, 12);
        addDataSlots(data);

        // 5 Permanent Dedicated Hopper Slots (0..4)
        // 0: Iron, 1: Gold, 2: Diamond, 3: Trinitite, 4: Tabbyrium
        // Active and interactive in Tab 1 (Resource Silos)
        for (int i = 0; i < 5; i++) {
            final ResourceValues.ResourceType targetType = ResourceValues.ResourceType.values()[i];
            hopperSlots[i] = new SlotItemHandler(hopperHandler, i, 22 + i * 36, 104) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ResourceValues.matchesType(stack, targetType);
                }

                @Override
                public boolean isActive() {
                    return currentTab == 1;
                }
            };
            this.addSlot(hopperSlots[i]);
        }

        // 1 Output Crate Slot (5) - active and interactive in Tab 0 (Production)
        outputSlot = new SlotItemHandler(hopperHandler, 5, 154, 104) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean isActive() {
                return currentTab == 0;
            }
        };
        this.addSlot(outputSlot);

        // Player Inventory (3 rows of 9) - centered in 204px chassis
        int startX = 21;
        int startY = 140;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        // Hotbar - start at y = 198
        int hotbarY = 198;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, startX + col * 18, hotbarY));
        }
    }

    public void setTab(int tabIndex) {
        this.currentTab = tabIndex;
    }

    public int getTab() {
        return currentTab;
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

    public int getIronTank() {
        return data.get(7);
    }

    public int getGoldTank() {
        return data.get(8);
    }

    public int getDiamondTank() {
        return data.get(9);
    }

    public int getTrinititeTank() {
        return data.get(10);
    }

    public int getTabbyriumTank() {
        return data.get(11);
    }

    public int getMaxTankCapacity() {
        return ResourceValues.MAX_TANK_CAPACITY;
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

            // Slots 0..5 are Hoppers + Output Crate Bay
            if (index < 6) {
                // Move from machine to player inventory
                if (!this.moveItemStackTo(slotStack, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory into matching dedicated hopper slot
                ResourceValues.ResourceType type = ResourceValues.getResourceType(slotStack);
                boolean moved = false;
                if (type != null) {
                    int targetSlot = type.ordinal(); // 0: Iron, 1: Gold, 2: Diamond, 3: Trinitite, 4: Tabbyrium
                    moved = this.moveItemStackTo(slotStack, targetSlot, targetSlot + 1, false);
                }

                if (!moved) {
                    if (index < 33) {
                        if (!this.moveItemStackTo(slotStack, 33, 42, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, 6, 33, false)) {
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
