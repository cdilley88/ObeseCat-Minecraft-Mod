package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class FoodBinItem extends BlockItem {
    public FoodBinItem(Block block, Properties properties) { super(block, properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.obesecat.food_bin.caption").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("block.obesecat.food_bin.role").withStyle(ChatFormatting.BLUE));
    }
}
