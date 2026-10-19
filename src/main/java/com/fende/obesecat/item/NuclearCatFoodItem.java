package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class NuclearCatFoodItem extends Item {
    private final int mealsProvided;

    public NuclearCatFoodItem(Properties properties, int mealsProvided) {
        super(properties);
        this.mealsProvided = mealsProvided;
    }

    public int getMealsProvided() {
        return mealsProvided;
    }

    public boolean isInstantDetonation() {
        return mealsProvided >= 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.obesecat.nuclear_cat_food.lore").withStyle(ChatFormatting.GOLD));
        if (isInstantDetonation()) {
            tooltip.add(Component.translatable("item.obesecat.nuclear_cat_food.instant").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("item.obesecat.nuclear_cat_food.threshold").withStyle(ChatFormatting.YELLOW));
        }
    }
}
