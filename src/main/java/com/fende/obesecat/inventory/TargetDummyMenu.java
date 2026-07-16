package com.fende.obesecat.inventory;

import com.fende.obesecat.entity.TargetDummy;
import com.fende.obesecat.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class TargetDummyMenu extends AbstractContainerMenu {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };
    private final TargetDummy dummy;

    public static TargetDummyMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var entity = inventory.player.level().getEntity(buffer.readVarInt());
        return new TargetDummyMenu(id, inventory, entity instanceof TargetDummy target ? target : null);
    }

    public TargetDummyMenu(int id, Inventory inventory, TargetDummy dummy) {
        super(ModMenus.TARGET_DUMMY.get(), id);
        this.dummy = dummy;
        for (int i = 0; i < SLOTS.length; i++) {
            final EquipmentSlot equipmentSlot = SLOTS[i];
            int x = i < 4 ? 44 : 116;
            int y = i < 4 ? 17 + i * 18 : 35 + (i - 4) * 36;
            addSlot(new Slot(new net.minecraft.world.SimpleContainer(1), 0, x, y) {
                @Override public ItemStack getItem() { return dummy == null ? ItemStack.EMPTY : dummy.getItemBySlot(equipmentSlot); }
                @Override public void set(ItemStack stack) { if (dummy != null) dummy.setItemSlot(equipmentSlot, stack); setChanged(); }
                @Override public ItemStack remove(int amount) {
                    ItemStack current = getItem();
                    if (current.isEmpty()) return ItemStack.EMPTY;
                    set(ItemStack.EMPTY);
                    return current;
                }
                @Override public boolean mayPlace(ItemStack stack) {
                    if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
                        return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == equipmentSlot;
                    return true;
                }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        for (int row=0; row<3; row++) for (int col=0; col<9; col++)
            addSlot(new Slot(inventory, col + row*9 + 9, 8 + col*18, 102 + row*18));
        for (int col=0; col<9; col++) addSlot(new Slot(inventory, col, 8 + col*18, 160));
    }

    @Override public boolean stillValid(Player player) { return dummy != null && dummy.isAlive() && player.distanceToSqr(dummy) < 64.0D; }
    public boolean isInfoCardEnabled() { return dummy != null && dummy.isInfoCardEnabled(); }

    public int getDummyEntityId() { return dummy == null ? -1 : dummy.getId(); }
    public void setInfoCardEnabled(boolean enabled) {
        if (dummy != null) dummy.setInfoCardEnabled(enabled);
    }
    public boolean controls(TargetDummy target) { return dummy == target; }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}


