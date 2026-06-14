package com.fende.obesecat.item;

import com.fende.obesecat.world.TinyPlanetProtection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TinyPlanetItem extends CaptionedItem {
    private static final int ACTIVE_TICKS = 200;
    private static final int COOLDOWN_TICKS = 600;
    private static final int HUNGER_COST = 2;

    public TinyPlanetItem(Properties properties) {
        super(properties, "item.obesecat.tiny_planet.caption");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() < HUNGER_COST) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.getAbilities().instabuild) {
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - HUNGER_COST));
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        TinyPlanetProtection.activate(serverPlayer, ACTIVE_TICKS);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.55F);

        return InteractionResultHolder.success(stack);
    }
}
