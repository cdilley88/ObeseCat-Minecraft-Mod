package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class CreativeFoodBinItem extends BlockItem {
    public CreativeFoodBinItem(Block block, Properties properties) { super(block, properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.obesecat.creative_food_bin.caption").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("block.obesecat.creative_food_bin.role").withStyle(ChatFormatting.BLUE));
    }
}
