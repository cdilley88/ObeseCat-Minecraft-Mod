package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class CatChargerItem extends BlockItem {
    public CatChargerItem(Block block, Properties properties) { super(block, properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.obesecat.cat_charger.caption").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("block.obesecat.cat_charger.role").withStyle(ChatFormatting.BLUE));
    }
}
