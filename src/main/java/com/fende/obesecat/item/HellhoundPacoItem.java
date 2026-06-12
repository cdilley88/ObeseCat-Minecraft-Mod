package com.fende.obesecat.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fende.obesecat.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HellhoundPacoItem extends PacoItem {
    private static final double FIRESTORM_LENGTH = 10.0D;
    private static final double HIT_RADIUS = 1.35D;
    private static final int STEPS = 28;
    private static final float DAMAGE = 5.0F;
    private static final int FIRE_SECONDS = 5;

    public HellhoundPacoItem(Properties properties) {
        super(properties);
    }

    @Override
    protected SoundEvent getBarkSound(Level level) {
        return switch (level.random.nextInt(3)) {
            case 0 -> ModSounds.PACO_HELLBARK_1.get();
            case 1 -> ModSounds.PACO_HELLBARK_2.get();
            default -> ModSounds.PACO_HELLBARK_3.get();
        };
    }

    @Override
    protected void applyBarkEffect(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 forward = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().subtract(0.0D, 0.35D, 0.0D);
        Vec3 side = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-5D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            side = side.normalize();
        }
        Vec3 up = side.cross(forward).normalize();

        Set<LivingEntity> hitEntities = new HashSet<>();
        for (int i = 0; i <= STEPS; i++) {
            double progress = (double) i / (double) STEPS;
            double distance = progress * FIRESTORM_LENGTH;
            double curl = Math.sin(progress * Math.PI * 4.5D) * 0.85D;
            double lift = Math.cos(progress * Math.PI * 3.0D) * 0.35D;
            Vec3 center = start
                    .add(forward.scale(distance))
                    .add(side.scale(curl))
                    .add(up.scale(lift));

            level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 8, 0.22D, 0.22D, 0.22D, 0.025D);
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 3, 0.18D, 0.18D, 0.18D, 0.01D);
            }
            if (i % 4 == 0) {
                level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z, 1, 0.08D, 0.08D, 0.08D, 0.0D);
            }

            AABB hitBox = new AABB(
                    center.x - HIT_RADIUS,
                    center.y - HIT_RADIUS,
                    center.z - HIT_RADIUS,
                    center.x + HIT_RADIUS,
                    center.y + HIT_RADIUS,
                    center.z + HIT_RADIUS
            );
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive() && !entity.isSpectator());
            for (LivingEntity target : targets) {
                if (hitEntities.add(target)) {
                    target.hurt(player.damageSources().onFire(), DAMAGE);
                    target.igniteForSeconds(FIRE_SECONDS);
                }
            }
        }
    }

    @Override
    protected boolean usesStinkMeter() {
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
