package com.fende.obesecat.world;

import com.fende.obesecat.item.PortableCatChargerItem;
import com.fende.obesecat.item.CreativePortableCatChargerItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class PortableCatChargerManager {
    private PortableCatChargerManager() {}

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (ServerPlayer player : level.players()) chargeHeldItems(player);
    }

    private static void chargeHeldItems(ServerPlayer player) {
        ItemStack charger = findActiveCharger(player);
        if (charger.isEmpty()) return;
        IEnergyStorage source = charger.getCapability(Capabilities.EnergyStorage.ITEM);
        if (source == null || source.getEnergyStored() <= 0) return;
        chargeHand(player, InteractionHand.MAIN_HAND, charger, source);
        chargeHand(player, InteractionHand.OFF_HAND, charger, source);
    }

    private static ItemStack findActiveCharger(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof PortableCatChargerItem && PortableCatChargerItem.isActive(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void chargeHand(ServerPlayer player, InteractionHand hand, ItemStack charger, IEnergyStorage source) {
        ItemStack targetStack = player.getItemInHand(hand);
        if (targetStack.isEmpty() || targetStack == charger || targetStack.getItem() instanceof PortableCatChargerItem) return;
        IEnergyStorage target = targetStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (target == null || !target.canReceive()) return;
        int transferRate = charger.getItem() instanceof CreativePortableCatChargerItem
                ? Integer.MAX_VALUE
                : PortableCatChargerItem.MAX_TRANSFER;
        int offered = source.extractEnergy(transferRate, true);
        int accepted = target.receiveEnergy(offered, true);
        if (accepted <= 0) return;
        int extracted = source.extractEnergy(accepted, false);
        target.receiveEnergy(extracted, false);
    }
}
