package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class OverdrivePowderItem extends Item {
    public OverdrivePowderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.obesecat.overdrive_powder.caption").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.obesecat.overdrive_powder.lore").withStyle(ChatFormatting.GRAY));
    }
}
