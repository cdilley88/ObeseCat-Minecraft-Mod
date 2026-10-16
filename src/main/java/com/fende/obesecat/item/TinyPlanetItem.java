package com.fende.obesecat.item;

import com.fende.obesecat.world.TinyPlanetProtection;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TinyPlanetItem extends CaptionedItem {
    public static final int ACTIVE_TICKS = 100; // 5 seconds
    public static final int COOLDOWN_TICKS = 300; // 15 seconds

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

        // Cannot activate if starving
        if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() <= 0) {
            player.displayClientMessage(Component.translatable("item.obesecat.tiny_planet.cannot_activate_starving"), true);
            return InteractionResultHolder.fail(stack);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        TinyPlanetProtection.activate(serverPlayer, ACTIVE_TICKS);

        // Orbital resonance activation audio
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.6F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 0.75F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LODESTONE_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F);

        player.displayClientMessage(Component.translatable("item.obesecat.tiny_planet.activated"), true);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.obesecat.tiny_planet.tooltip_duration"));
        tooltipComponents.add(Component.translatable("item.obesecat.tiny_planet.tooltip_shield"));
        tooltipComponents.add(Component.translatable("item.obesecat.tiny_planet.tooltip_hunger"));
        tooltipComponents.add(Component.translatable("item.obesecat.tiny_planet.tooltip_cooldown"));
    }
}
