package com.fende.obesecat.item;

import com.fende.obesecat.block.entity.TabbyriumSiloBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

public class TabbyriumSiloItem extends BlockItem {
    public TabbyriumSiloItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static int getStoredEnergy(ItemStack stack) {
        CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null && blockEntityData.contains("Energy")) {
            return blockEntityData.copyTag().getInt("Energy");
        }
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom != null && custom.contains("Energy")) {
            return custom.copyTag().getInt("Energy");
        }
        return 0;
    }

    public static void setStoredEnergy(ItemStack stack, int energy) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Energy", energy);
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredEnergy(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int stored = getStoredEnergy(stack);
        return Math.round(13.0F * stored / TabbyriumSiloBlockEntity.CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x28FF38; // Glowing Tiberium Green
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.obesecat.tabbyrium_silo.caption").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("block.obesecat.tabbyrium_silo.role").withStyle(ChatFormatting.GREEN));
        int stored = getStoredEnergy(stack);
        if (stored > 0) {
            tooltip.add(Component.literal(String.format("§aCharge: %,d / %,d FE", stored, TabbyriumSiloBlockEntity.CAPACITY)));
        }
        tooltip.add(Component.literal("§8Integrated induction charging slot").withStyle(ChatFormatting.DARK_GRAY));
    }
}
