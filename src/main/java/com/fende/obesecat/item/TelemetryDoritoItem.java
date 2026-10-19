package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class TelemetryDoritoItem extends CaptionedItem {
    public static final String ACTIVE_KEY = "TelemetryDoritoActive";

    public TelemetryDoritoItem(Properties properties) {
        super(properties, "item.obesecat.telemetry_dorito.caption");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            boolean nextState = !isActive(stack);
            setActive(stack, nextState);
            player.displayClientMessage(
                    Component.translatable(nextState ? "item.obesecat.telemetry_dorito.active" : "item.obesecat.telemetry_dorito.inactive"),
                    true
            );
            if (nextState) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.9F, 1.8F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.35F, 2.0F);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 0.7F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.3F, 1.6F);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        boolean active = isActive(stack);
        tooltipComponents.add(Component.translatable(active ? "item.obesecat.telemetry_dorito.tooltip_active" : "item.obesecat.telemetry_dorito.tooltip_inactive"));
        tooltipComponents.add(Component.translatable("item.obesecat.telemetry_dorito.tooltip_desc"));
        tooltipComponents.add(Component.translatable("item.obesecat.telemetry_dorito.tooltip_curios"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    public static boolean isActive(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        CompoundTag tag = customData.copyTag();
        return tag.getBoolean(ACTIVE_KEY);
    }

    public static void setActive(ItemStack stack, boolean active) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (active) {
            tag.putBoolean(ACTIVE_KEY, true);
        } else {
            tag.remove(ACTIVE_KEY);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
