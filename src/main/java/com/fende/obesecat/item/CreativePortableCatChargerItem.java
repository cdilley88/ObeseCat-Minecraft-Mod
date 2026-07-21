package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CreativePortableCatChargerItem extends PortableCatChargerItem {
    public CreativePortableCatChargerItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.obesecat.creative_portable_cat_charger.caption").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.obesecat.creative_portable_cat_charger.role").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.obesecat.creative_portable_cat_charger.energy").withStyle(ChatFormatting.GOLD));
    }

    public static IEnergyStorage createCreativeEnergyStorage() {
        return new IEnergyStorage() {
            @Override public int receiveEnergy(int maxReceive, boolean simulate) { return Math.max(maxReceive, 0); }
            @Override public int extractEnergy(int maxExtract, boolean simulate) { return Math.max(maxExtract, 0); }
            @Override public int getEnergyStored() { return Integer.MAX_VALUE; }
            @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
            @Override public boolean canExtract() { return true; }
            @Override public boolean canReceive() { return true; }
        };
    }
}
