package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class AtomicCanOpenerItem extends BlockItem {
    public AtomicCanOpenerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.obesecat.can_opener.caption").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("block.obesecat.can_opener.role").withStyle(ChatFormatting.BLUE));
    }
}
