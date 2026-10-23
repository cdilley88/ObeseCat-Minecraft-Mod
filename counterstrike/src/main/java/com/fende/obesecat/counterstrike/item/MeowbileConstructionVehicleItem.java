package com.fende.obesecat.counterstrike.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class MeowbileConstructionVehicleItem extends BlockItem {

    public MeowbileConstructionVehicleItem(Block block, Properties properties) {
        super(block, properties);
    }

    public MeowbileConstructionVehicleItem(Block block) {
        this(block, new Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6M.C.V. — MEOW-BILE CONSTRUCTION VEHICLE").withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("§7\"Packing up and moving out!\"").withStyle(ChatFormatting.ITALIC));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("§e[Place] §fPosition in world, then right-click with §6Command Cursor"));
        tooltipComponents.add(Component.literal("§a[Deploys] §f5×4×4 Feline Construction Yard"));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("§8Primary command center for all Counterstrike tech schematics.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
