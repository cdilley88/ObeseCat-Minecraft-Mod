package com.fende.obesecat.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class NightVisionMrKittyItem extends CaptionedItem {
    private static final String ACTIVE_KEY = "NightVisionMrKittyActive";

    public NightVisionMrKittyItem(Properties properties) {
        super(properties, "item.obesecat.night_vision_mr_kitty.caption");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            setActive(stack, !isActive(stack));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBoolean(ACTIVE_KEY);
    }

    private static void setActive(ItemStack stack, boolean active) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (active) {
                tag.putBoolean(ACTIVE_KEY, true);
            } else {
                tag.remove(ACTIVE_KEY);
            }
        });
    }
}
