package com.fende.obesecat.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public class WormholeEmberItem extends CaptionedItem {
    public WormholeEmberItem(Properties properties) {
        super(properties, "item.obesecat.wormhole_ember.caption");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!player.getAbilities().instabuild && serverPlayer.getFoodData().getFoodLevel() < 6) {
            return InteractionResultHolder.fail(stack);
        }

        sendDepartureEffects(serverPlayer);
        DimensionTransition transition = serverPlayer.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING);
        Vec3 destination = transition.pos();
        serverPlayer.teleportTo(transition.newLevel(), destination.x, destination.y, destination.z, transition.yRot(), transition.xRot());
        if (!serverPlayer.getAbilities().instabuild) {
            int newFood = Math.max(0, serverPlayer.getFoodData().getFoodLevel() - 6);
            serverPlayer.getFoodData().setFoodLevel(newFood);
            serverPlayer.getFoodData().setSaturation(Math.min(serverPlayer.getFoodData().getSaturationLevel(), (float) newFood));
        }
        player.getCooldowns().addCooldown(this, 100);
        sendArrivalEffects(serverPlayer);

        return InteractionResultHolder.success(stack);
    }

    public static void sendDepartureEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.75F);
        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 48, 0.45D, 0.85D, 0.45D, 0.35D);
        level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.6D, player.getZ(), 16, 0.35D, 0.35D, 0.35D, 0.04D);
    }

    public static void sendArrivalEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.15F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 64, 0.5D, 0.9D, 0.5D, 0.2D);
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.25D, player.getZ(), 12, 0.35D, 0.12D, 0.35D, 0.02D);
    }
}
