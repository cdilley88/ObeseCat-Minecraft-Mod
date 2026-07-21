package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class EchoingBlastChamberItem extends BlockItem {
    public EchoingBlastChamberItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        for (int line = 1; line <= 1; line++) {
            tooltip.add(Component.translatable("block.obesecat.echoing_blast_chamber.lore." + line)
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
