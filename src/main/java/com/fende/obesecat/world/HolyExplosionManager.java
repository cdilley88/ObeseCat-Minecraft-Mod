package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Vector3f;

public final class HolyExplosionManager {
    public static final double RANGE = 25.0D;
    public static final double EXPLOSION_RADIUS = 5.0D;
    public static final double PILLAR_ORBIT_RADIUS = 2.5D;
    public static final float EXPLOSION_DAMAGE = 10.0F;
    public static final float PILLAR_DAMAGE = 1.5F;
    public static final int CAST_DELAY_TICKS = 12;
    public static final int PILLAR_DURATION_TICKS = 50;
    public static final int COOLDOWN_TICKS = 90;

    private static final List<PendingCast> PENDING_CASTS = new ArrayList<>();
    private static final List<PendingPillars> PENDING_PILLARS = new ArrayList<>();

    private HolyExplosionManager() {
    }

    public static void schedule(ServerLevel level, BlockPos origin) {
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), CAST_DELAY_TICKS));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (PENDING_CASTS.isEmpty() && PENDING_PILLARS.isEmpty()) {
            return;
        }

        List<PendingCast> pendingCasts = List.copyOf(PENDING_CASTS);
        for (PendingCast pending : pendingCasts) {
            if (pending.level != level) {
                continue;
            }
            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                PENDING_CASTS.remove(pending);
                executeCast(level, pending.origin);
            }
        }

        List<PendingPillars> pendingPillars = List.copyOf(PENDING_PILLARS);
        for (PendingPillars pending : pendingPillars) {
            if (pending.level != level) {
                continue;
            }

            emitRotatingPillars(level, pending.origin, pending.ticksRemaining);
            if (pending.ticksRemaining % 5 == 0) {
                damagePillarZones(level, pending.origin, pending.ticksRemaining);
            }

            pending.ticksRemaining--;
            if (pending.ticksRemaining <= 0) {
                PENDING_PILLARS.remove(pending);
            }
        }
    }

    private static void executeCast(ServerLevel level, BlockPos origin) {
        Vec3 center = Vec3.atCenterOf(origin);
        LocalSoundHelper.playLocalized(level, center, ModSounds.HOLY_EXPLOSION.get(), 36.0D, 1.0F, 1.0F);

        AABB box = new AABB(
                center.x - EXPLOSION_RADIUS, center.y - 2.0D, center.z - EXPLOSION_RADIUS,
                center.x + EXPLOSION_RADIUS, center.y + 4.0D, center.z + EXPLOSION_RADIUS
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && !(e instanceof Player))) {
            entity.hurt(level.damageSources().magic(), EXPLOSION_DAMAGE);
        }

        PENDING_PILLARS.add(new PendingPillars(level, origin.immutable(), PILLAR_DURATION_TICKS));
    }

    private static void emitRotatingPillars(ServerLevel level, BlockPos origin, int ticksRemaining) {
        int progressTicks = PILLAR_DURATION_TICKS - ticksRemaining;
        double angle = progressTicks * 0.24D;

        Vec3 center = Vec3.atCenterOf(origin);
        Vec3 first = new Vec3(
                center.x + Math.cos(angle) * PILLAR_ORBIT_RADIUS,
                center.y,
                center.z + Math.sin(angle) * PILLAR_ORBIT_RADIUS
        );
        Vec3 second = new Vec3(
                center.x + Math.cos(angle + Math.PI) * PILLAR_ORBIT_RADIUS,
                center.y,
                center.z + Math.sin(angle + Math.PI) * PILLAR_ORBIT_RADIUS
        );

        float warmPulseA = (float) (0.5D + 0.5D * Math.sin(progressTicks * 0.2D));
        float warmPulseB = (float) (0.5D + 0.5D * Math.sin(progressTicks * 0.2D + Math.PI));
        spawnPillar(level, first, warmPulseA);
        spawnPillar(level, second, warmPulseB);
    }

    private static void spawnPillar(ServerLevel level, Vec3 base, float warmPulse) {
        Vector3f paleGold = new Vector3f(1.0F, 0.96F, 0.70F);
        Vector3f richGold = new Vector3f(1.0F, 0.78F, 0.25F);
        Vector3f rgb = new Vector3f(
                lerp(paleGold.x, richGold.x, warmPulse),
                lerp(paleGold.y, richGold.y, warmPulse),
                lerp(paleGold.z, richGold.z, warmPulse)
        );
        DustParticleOptions glow = new DustParticleOptions(rgb, 1.4F);

        for (int y = 0; y < 16; y++) {
            double yy = base.y + 0.2D + y * 0.42D;
            level.sendParticles(glow, base.x, yy, base.z, 2, 0.12D, 0.16D, 0.12D, 0.0D);
            level.sendParticles(ParticleTypes.END_ROD, base.x, yy, base.z, 1, 0.08D, 0.1D, 0.08D, 0.0D);
        }

        level.sendParticles(ParticleTypes.GLOW, base.x, base.y + 7.0D, base.z, 4, 0.3D, 0.8D, 0.3D, 0.0D);
    }

    private static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private static void damagePillarZones(ServerLevel level, BlockPos origin, int ticksRemaining) {
        int progressTicks = PILLAR_DURATION_TICKS - ticksRemaining;
        double angle = progressTicks * 0.24D;
        Vec3 center = Vec3.atCenterOf(origin);

        Vec3 first = new Vec3(
                center.x + Math.cos(angle) * PILLAR_ORBIT_RADIUS,
                center.y + 1.0D,
                center.z + Math.sin(angle) * PILLAR_ORBIT_RADIUS
        );
        Vec3 second = new Vec3(
                center.x + Math.cos(angle + Math.PI) * PILLAR_ORBIT_RADIUS,
                center.y + 1.0D,
                center.z + Math.sin(angle + Math.PI) * PILLAR_ORBIT_RADIUS
        );

        damageAround(level, first);
        damageAround(level, second);
    }

    private static void damageAround(ServerLevel level, Vec3 center) {
        AABB box = new AABB(
                center.x - 1.3D, center.y - 1.0D, center.z - 1.3D,
                center.x + 1.3D, center.y + 3.0D, center.z + 1.3D
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && !(e instanceof Player))) {
            entity.hurt(level.damageSources().magic(), PILLAR_DAMAGE);
        }
    }

    private static final class PendingCast {
        private final ServerLevel level;
        private final BlockPos origin;
        private int delayTicks;

        private PendingCast(ServerLevel level, BlockPos origin, int delayTicks) {
            this.level = level;
            this.origin = origin;
            this.delayTicks = delayTicks;
        }
    }

    private static final class PendingPillars {
        private final ServerLevel level;
        private final BlockPos origin;
        private int ticksRemaining;

        private PendingPillars(ServerLevel level, BlockPos origin, int ticksRemaining) {
            this.level = level;
            this.origin = origin;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
