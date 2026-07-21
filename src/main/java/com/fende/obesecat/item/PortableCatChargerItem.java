package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class PortableCatChargerItem extends Item {
    public static final int CAPACITY = 500_000;
    public static final int MAX_TRANSFER = 1_000;
    private static final String ENERGY_KEY = "PortableCatChargerEnergy";
    private static final String ACTIVE_KEY = "PortableCatChargerActive";

    public PortableCatChargerItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) setActive(stack, !isActive(stack));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override public boolean isFoil(ItemStack stack) { return isActive(stack); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.obesecat.portable_cat_charger.caption").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.obesecat.portable_cat_charger.role").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.obesecat.portable_cat_charger.energy", getEnergy(stack), CAPACITY)
                .withStyle(ChatFormatting.GRAY));
    }

    public static IEnergyStorage createEnergyStorage(ItemStack stack) { return new StackEnergyStorage(stack); }
    public static boolean isActive(ItemStack stack) { return getTag(stack).getBoolean(ACTIVE_KEY); }
    public static int getEnergy(ItemStack stack) { return Math.max(0, Math.min(CAPACITY, getTag(stack).getInt(ENERGY_KEY))); }

    private static void setActive(ItemStack stack, boolean active) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (active) tag.putBoolean(ACTIVE_KEY, true); else tag.remove(ACTIVE_KEY);
        });
    }

    private static void setEnergy(ItemStack stack, int energy) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(ENERGY_KEY, Math.max(0, Math.min(CAPACITY, energy))));
    }

    private static CompoundTag getTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private record StackEnergyStorage(ItemStack stack) implements IEnergyStorage {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int stored = getEnergy(stack);
            int received = Math.min(Math.max(maxReceive, 0), Math.min(CAPACITY - stored, MAX_TRANSFER));
            if (!simulate && received > 0) setEnergy(stack, stored + received);
            return received;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int stored = getEnergy(stack);
            int extracted = Math.min(Math.max(maxExtract, 0), Math.min(stored, MAX_TRANSFER));
            if (!simulate && extracted > 0) setEnergy(stack, stored - extracted);
            return extracted;
        }
        @Override public int getEnergyStored() { return getEnergy(stack); }
        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    }
}
