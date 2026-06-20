package com.fende.obesecat.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class SniperPacoItem extends PacoItem {
    private static final double SNIPER_RANGE = 50.0D;
    private static final float SNIPER_DAMAGE = 16.0F;
    private static final int RELOAD_TICKS = 50;
    private static final String LOADED_KEY = "SniperPacoLoaded";

    public SniperPacoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!isLoaded(stack)) {
            if (!level.isClientSide()) {
                setLoaded(stack, true);
                player.getCooldowns().addCooldown(this, RELOAD_TICKS);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!level.isClientSide()) {
            SoundEvent bark = getBarkSound(level);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), bark, SoundSource.PLAYERS, 1.0F, 0.88F + (level.random.nextFloat() * 0.08F));

            LivingEntity target = findTarget(player);
            if (target != null) {
                target.hurt(player.damageSources().playerAttack(player), SNIPER_DAMAGE);
            }

            setLoaded(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    protected double getRange() {
        return SNIPER_RANGE;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    protected String getCaptionKey() {
        return "item.obesecat.sniper_paco.caption";
    }

    private static boolean isLoaded(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !tag.contains(LOADED_KEY) || tag.getBoolean(LOADED_KEY);
    }

    private static void setLoaded(ItemStack stack, boolean loaded) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (loaded) {
                tag.remove(LOADED_KEY);
            } else {
                tag.putBoolean(LOADED_KEY, false);
            }
        });
    }
}
