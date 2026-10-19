package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class OverdriveRodItem extends Item {
    public OverdriveRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.obesecat.overdrive_rod.caption").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.obesecat.overdrive_rod.lore").withStyle(ChatFormatting.AQUA));
    }
}
