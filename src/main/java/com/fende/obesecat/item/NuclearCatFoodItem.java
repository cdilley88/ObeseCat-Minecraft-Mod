package com.fende.obesecat.item;

import com.fende.obesecat.energy.CatFoodEnergy;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class NuclearCatFoodItem extends Item implements CatFoodFuel {
    private final int catFoodPoints;

    public NuclearCatFoodItem(Properties properties, int catFoodPoints) {
        super(properties);
        this.catFoodPoints = catFoodPoints;
    }

    @Override
    public int getCatFoodPoints() {
        return catFoodPoints;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "item.obesecat.nuclear_cat_food.charge",
                catFoodPoints,
                CatFoodEnergy.toFe(catFoodPoints)
        ).withStyle(ChatFormatting.GOLD));
    }
}
