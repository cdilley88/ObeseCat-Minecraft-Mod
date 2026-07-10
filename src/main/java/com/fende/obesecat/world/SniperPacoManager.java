package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public final class SniperPacoManager {
    private static final double SNIPER_RANGE = 150.0D;
    private static final float SNIPER_DAMAGE = 8.0F;

    private SniperPacoManager() {
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isZoomingSniper(player)) {
            return;
        }
        event.setCanceled(true);
    }

    public static void queueShot(ServerPlayer player) {
        if (!isZoomingSniper(player)) {
            return;
        }

        long gameTime = player.serverLevel().getGameTime();
        int cooldownTicks = SniperPacoState.consumeShot(player.getUseItem(), gameTime);
        if (cooldownTicks < 0) {
            return;
        }

        LivingEntity target = findTarget(player);
        if (target != null) {
            target.hurt(player.damageSources().playerAttack(player), SNIPER_DAMAGE);
        }
        player.getCooldowns().addCooldown(ModItems.SNIPER_PACO.get(), cooldownTicks);
        player.serverLevel().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                switch (player.serverLevel().random.nextInt(3)) {
                    case 0 -> ModSounds.PACO_BARK_1.get();
                    case 1 -> ModSounds.PACO_BARK_2.get();
                    default -> ModSounds.PACO_BARK_3.get();
                },
                SoundSource.PLAYERS,
                1.0F,
                0.98F
        );
    }

    private static boolean isZoomingSniper(Player player) {
        return player.isUsingItem() && player.getUseItem().is(ModItems.SNIPER_PACO.get());
    }

    private static LivingEntity findTarget(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(SNIPER_RANGE));
        HitResult blockHit = player.pick(SNIPER_RANGE, 1.0F, false);
        double maxDistance = blockHit.getType() == HitResult.Type.MISS
                ? SNIPER_RANGE * SNIPER_RANGE
                : blockHit.getLocation().distanceToSqr(start);
        AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                searchBox,
                entity -> entity instanceof LivingEntity livingEntity
                        && entity != player
                        && entity.isAlive()
                        && entity.isPickable()
                        && !entity.isSpectator(),
                maxDistance
        );

        return entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null;
    }
}
