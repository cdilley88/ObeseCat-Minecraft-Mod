package com.fende.obesecat.inventory;

import com.fende.obesecat.block.entity.EchoingBlastChamberBlockEntity;
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

public class EchoingBlastChamberMenu extends AbstractContainerMenu {
    private final Container chamber;
    private final ContainerData data;

    public static EchoingBlastChamberMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        buffer.readBlockPos();
        return new EchoingBlastChamberMenu(id, inventory, new SimpleContainer(EchoingBlastChamberBlockEntity.SLOT_COUNT), new SimpleContainerData(2));
    }

    public EchoingBlastChamberMenu(int id, Inventory inventory, Container chamber, ContainerData data) {
        super(ModMenus.ECHOING_BLAST_CHAMBER.get(), id);
        this.chamber = chamber;
        this.data = data;
        checkContainerSize(chamber, EchoingBlastChamberBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 2);
        chamber.startOpen(inventory.player);
        addSlot(new Slot(chamber, 0, 44, 35));
        addSlot(new Slot(chamber, 1, 44, 58));
        addSlot(new Slot(chamber, 2, 116, 47) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addDataSlots(data);
    }

    public int progressWidth() { int total = data.get(1); return total <= 0 ? 0 : data.get(0) * 24 / total; }
    public boolean isProcessing() { return data.get(0) > 0; }
    @Override public boolean stillValid(Player player) { return chamber.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); chamber.stopOpen(player); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < 3) {
            if (!moveItemStackTo(source, 3, slots.size(), true)) return ItemStack.EMPTY;
        } else if (chamber.canPlaceItem(0, source)) {
            if (!moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY;
        } else if (chamber.canPlaceItem(1, source)) {
            if (!moveItemStackTo(source, 1, 2, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
}
